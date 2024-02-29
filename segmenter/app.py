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

app = Flask("Segmenter")

@app.route("/segment", methods=["POST"])
def segment():
    img = stream_to_img(request.stream)

    if img is None:
        return Response("missing or invalid photo", status=400)

    # Rescale to constant size so the epsilon value of approxPolyDP can give
    # consistent results.
    img = img_util.scale_smaller_axis(img, 480)

    bounds = models.predict_bounds(img)
    if bounds is None:
        app.logger.info("bounds were not found on image")
        if app.debug:
            img_util.debug_write(img, "bounds")

        return SCAN_ERROR_RESPONSE

    if app.debug:
        debug_img = img.copy()
        cv2.rectangle(debug_img, (bounds[0], bounds[1]), (bounds[2], bounds[3]), color=(255, 255, 255))

    # Increasing bounding box size slightly increases segmentation accuracy
    segmentation_bounds = (
        bounds[0] - SEGMENTATION_BOUND_PADDING,
        bounds[1] - SEGMENTATION_BOUND_PADDING,
        bounds[2] + SEGMENTATION_BOUND_PADDING,
        bounds[3] + SEGMENTATION_BOUND_PADDING,
    )
    contour_points = models.predict_segmentation(img, segmentation_bounds)
    if contour_points is None:
        app.logger.info("segmentation was not found on image")
        if app.debug:
            img_util.debug_write(debug_img, "segmentation") # pyright: ignore

        return SCAN_ERROR_RESPONSE

    if app.debug:
        cv2.polylines(debug_img, [contour_points], -1, color=(0, 255, 0)) # pyright: ignore

    contour_points = reduce_segmentation(contour_points)
    if app.debug:
        for point in contour_points:
            cv2.circle(debug_img, point, radius=4, color=(255, 0, 0), thickness=-1) # pyright: ignore

    if len(contour_points) != 6:
        app.logger.info("contour not reduced")
        if app.debug:
            img_util.debug_write(debug_img, "reduce") # pyright: ignore

        return SCAN_ERROR_RESPONSE

    highest = index_of_lowest_y(contour_points) # "highest" is lowest y because top of image is y=0
    height, width, _ = img.shape
    encoded_points = [{"x": p[0] / width, "y": p[1] / height} for p in contour_points.tolist()]
    keys = ["top", "topLeft", "bottomLeft", "bottom", "bottomRight", "topRight"]

    response = {}
    for offset in range(6):
        key = keys[offset]
        value = encoded_points[(highest + offset) % len(contour_points)]
        response[key] = value

    return jsonify(response)

app.run(debug=True)
