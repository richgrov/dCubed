import numpy as np
from ultralytics import FastSAM, YOLO
from ultralytics.models.fastsam import FastSAMPrompt
from transformers import pipeline
from PIL import Image
import cv2

object_model = YOLO("best.onnx")
segment_model = FastSAM(model="FastSAM-x.pt")
pipe = pipeline(task="depth-estimation", model="LiheYoung/depth-anything-small-hf")

BoundingBox = tuple[int, int, int, int]


def predict_bounds(img) -> BoundingBox | None:
    """
    Predicts the (minx, miny, maxx, maxy) boundary of a Rubik's cube in the
    image. Returns None if nothing was found.
    """
    results = object_model(cv2.cvtColor(img, cv2.COLOR_BGR2RGB))
    if len(results) == 0:
        return

    if len(results[0].boxes) == 0:
        return

    bounds = results[0].boxes[0].xyxy[0]
    return bounds.cpu().numpy().astype(np.int32).tolist()


def predict_segmentation(img, bounds: BoundingBox):
    """
    Returns a 2xN array of points that make up a contour of the object
    identified with the bounding box of an image. Returns None if nothing
    was found.
    """
    results = segment_model(img)
    process = FastSAMPrompt(img, results)
    results = process.box_prompt(bounds)
    if len(results) > 0:
        contour_points = results[0].masks.xy[0]
        return np.array(contour_points, np.int32)


def predict_depth(img):
    pil_img = Image.fromarray(img)
    return pipe(pil_img)["depth"]
