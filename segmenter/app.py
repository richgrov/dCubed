import sys
import typing

import numpy as np
import cv2
from flask import Flask, Response, request, jsonify

import img_util
import models

SCAN_ERROR_RESPONSE = Response("cube not found", status=422)
SEGMENTATION_BOUND_PADDING = 15

def stream_to_img(stream: typing.IO[bytes]):
    buf = bytearray(stream.read())
    npbuf = np.asarray(buf, np.uint8)
    return cv2.imdecode(npbuf, cv2.IMREAD_COLOR)

def reduce_segmentation(contour_points):
    approx = cv2.approxPolyDP(contour_points, epsilon=15, closed=True)
    return np.squeeze(approx, axis=1)

def index_of_lowest_y(points):
    lowest_index = -1
    lowest_y = sys.maxsize

    for i, (_, y) in enumerate(points):
        if y < lowest_y:
            lowest_index = i
            lowest_y = y

    return lowest_index

def rescale_point(point, current_img, new_img) -> tuple[float, float]:
    current_height, current_width, _ = current_img.shape
    new_height, new_width, _ = new_img.shape

    x, y = point
    return x / current_width * new_width, y / current_height * new_height

def find_center(img, mask) -> tuple[int, int]:
    depth = models.predict_depth(img)
    cv2_depth = np.array(depth)

    _, _, _, maxLoc = cv2.minMaxLoc(cv2_depth, mask)
    return maxLoc[0], maxLoc[1]

class VisionError(RuntimeError):
    def __init__(self, *args: object) -> None:
        super().__init__(*args)

def find_points(img, debug):
    # Rescale to constant size so the epsilon value of approxPolyDP can give
    # consistent results.
    scaled = img_util.scale_smaller_axis(img, 480)

    bounds = models.predict_bounds(scaled)
    if bounds is None:
        if debug:
            img_util.debug_write(scaled, "bounds")

        raise VisionError("bounds were not found on image")

    if debug:
        debug_img = scaled.copy()
        cv2.rectangle(debug_img, (bounds[0], bounds[1]), (bounds[2], bounds[3]), color=(255, 255, 255))

    # Increasing bounding box size slightly increases segmentation accuracy
    segmentation_bounds = (
        bounds[0] - SEGMENTATION_BOUND_PADDING,
        bounds[1] - SEGMENTATION_BOUND_PADDING,
        bounds[2] + SEGMENTATION_BOUND_PADDING,
        bounds[3] + SEGMENTATION_BOUND_PADDING,
    )
    contour_points = models.predict_segmentation(scaled, segmentation_bounds)
    if contour_points is None:
        if debug:
            img_util.debug_write(debug_img, "segmentation") # pyright: ignore

        raise VisionError("segmentation was not found on image")

    if debug:
        cv2.polylines(debug_img, [contour_points], -1, color=(0, 255, 0)) # pyright: ignore

    contour_points = reduce_segmentation(contour_points)
    if debug:
        for point in contour_points:
            cv2.circle(debug_img, point, radius=4, color=(255, 0, 0), thickness=-1) # pyright: ignore

    if len(contour_points) != 6:
        if debug:
            img_util.debug_write(debug_img, "reduce") # pyright: ignore

        raise VisionError("contour not reduced")

    highest = index_of_lowest_y(contour_points) # "highest" is lowest y because top of image is y=0

    mask = np.zeros((scaled.shape[0], scaled.shape[1]), np.uint8)
    cv2.drawContours(mask, [contour_points], -1, (255, 255, 255), -1)

    cropped = scaled[bounds[1]:bounds[3], bounds[0]:bounds[2]].copy()
    cropped_mask = mask[bounds[1]:bounds[3], bounds[0]:bounds[2]].copy()
    center = find_center(cropped, cropped_mask)
    center = center[0] + bounds[0], center[1] + bounds[1]

    # Point needs to be rescaled to match dimensions of input image
    rescaled = [rescale_point(p, scaled, img) for p in contour_points.tolist()]
    keys = ["top", "topLeft", "bottomLeft", "bottom", "bottomRight", "topRight"]

    response = {}
    for offset in range(6):
        key = keys[offset]
        value = rescaled[(highest + offset) % len(contour_points)]
        response[key] = value

    response["center"] = center[0] + bounds[0], center[1] + bounds[1]

    return response

app = Flask("Segmenter")

@app.route("/segment", methods=["POST"])
def segment():
    img = stream_to_img(request.stream)

    if img is None:
        return Response("missing or invalid photo", status=400)

    try:
        points = find_points(img, app.debug)
        points_dict = { key: {"x": p[0], "y": p[1]} for key, p in points.items() }
        return jsonify(points_dict)

    except VisionError as e:
        app.logger.info(e)

    return SCAN_ERROR_RESPONSE

app.run(debug=True)

