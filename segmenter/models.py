from inference.models.yolo_world import YOLOWorld
import numpy as np
from ultralytics import FastSAM
from ultralytics.models.fastsam import FastSAMPrompt

object_model = YOLOWorld(model_id="yolo_world/l")
object_model.set_classes(["rubik's cube"])

segment_model = FastSAM(model="FastSAM-x.pt")

BoundingBox = tuple[int, int, int, int]

def predict_bounds(img) -> BoundingBox | None:
    """
        Predicts the (minx, miny, maxx, maxy) boundary of a Rubik's cube in the
        image. Returns None if nothing was found.
    """
    results = object_model.infer(img, confidence=0.01)
    if len(results.predictions) > 0:
        bounds = results.predictions[0]
        x1 = int(bounds.x - bounds.width / 2)
        x2 = int(bounds.x + bounds.width / 2)
        y1 = int(bounds.y - bounds.height / 2)
        y2 = int(bounds.y + bounds.height / 2)
        return x1, y1, x2, y2

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
