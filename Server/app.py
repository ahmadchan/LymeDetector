import base64
import numpy as np
import io
from PIL import Image
import tensorflow
import keras
from keras import backend as K
from keras.models import Sequential, load_model
from keras.preprocessing.image import ImageDataGenerator, img_to_array
from flask import Flask
from flask import jsonify
from flask import request
from flask import render_template
from flask_cors import CORS

from imageio import imread

app = Flask(__name__)  # Loading Flask
CORS(app)


@app.route('/')
def index():
    return render_template("predict.html")


def get_model():  # Loading Tensorflow model
    global model
    model = load_model("model_600x600_83percent.h5")
    print("Model loaded!")


def preprocess_image(image, target_size):  # Preprocessing the image (correct dimensions, turn into a tensor
    if image.mode != 'RGB':
        image = image.convert("RGB")
    image = image.resize(target_size)
    image = img_to_array(image)
    image = np.array([image])
    print(str(image.shape))
    # image = np.expand_dims(image, axis=0)

    return image


print("Loading Keras model...")
get_model()
print("Model Loaded!")

from tensorflow.python.keras.preprocessing import image


# FOR TESTING
# x = image.load_img('bull_182.jpg', target_size=(600, 600))
# x = image.img_to_array(x)
# x = x.reshape((1,) + x.shape)
# x = x / 255.
# classes = model.predict(x)
# print(classes)


@app.route("/predict", methods=["POST"])
def predict():
    message = request.get_json(force=True)  # Image is loaded in JSON format
    print(message)
    encoded = message['image']  # Get the image in Base64 format from JSON

    encoded = encoded.replace("\n", "")  # Base64 Android encoder/Python decoder differ a bit, this resolves conflicts.
    # encoded = encoded.split(",")[-1]   IF YOU WANT TO WORK IN BROWSER UNCOMMENT (android will not work)
    print(encoded)

    # img = imread(io.BytesIO(base64.b64decode((message['image'])))) IF YOU WANT TO WORK IN BROWSER UNCOMMENT (android will not work)

    decoded = base64.b64decode(encoded)  # Decoding Base64 into an actual image

    fh = open("out.jpg", "wb")  # Save the image
    fh.write(decoded)
    fh.close()

    ##

    from tensorflow.python.keras.preprocessing import image

    x = image.load_img('out.jpg', target_size=(600, 600))  # Open image as Tensorflow/Keras image
    x = image.img_to_array(x)  # Turn into an array
    x = x.reshape((1,) + x.shape)  # Turn into a tensor
    x = x / 255.  # Each pixel now has a value between 0 and 1
    prediction_1 = model.predict(x)  # classify
    print(prediction_1)

    # processed_image = preprocess_image(unprocessed_image, target_size=(600, 600))

    # for pixel in processed_image:
    # processed_image.prod()

    response = str('{0:.10f}'.format(prediction_1[0][0]))  # prediction[0][0] is where the prediction of Lyme lies.
                                                           # predcition[0][1] is where the prediction of not lyme lies
    return jsonify(response)  # prediction returned to Android as a string in JSON


if __name__ == '__main__':
    app.run()
