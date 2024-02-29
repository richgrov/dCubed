from datetime import datetime

import cv2

def scale_smaller_axis(img, axis_size: int):
    """
        Scales the smaller axis of an image to the specified size while
        maintaining the original aspect ratio. For example, an 1920x1080 image
        with axis_size=480 will return an image with size 853x480. If the image
        was 1080x1920, the returned image would be 480x853.
    """
    height, width, _ = img.shape
    aspect = width / height

    if width < height:
        new_width = axis_size
        new_height = new_width / aspect
    else:
        new_height = axis_size
        new_width = new_height * aspect

    return cv2.resize(img, (int(new_width), int(new_height)))

def debug_write(img, step: str):
    time_str = datetime.now().strftime("%Y-%m-%d-%H-%M-%S")
    if not cv2.imwrite(step + "-" + time_str + ".jpg", img):
        raise RuntimeError("failed to save")
