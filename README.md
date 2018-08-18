# LymeDetector
Android app to recognize Lyme Disease using machine learning.

## How to deploy
Lyme Detector is a client-server application. To deploy it yourself there are two parts:
## Server part:
* Download the server part to your server
* Install Tensorflow, Keras, Flask, flask-cors, numpy, pillow, imageio, matplotlib, python-tk  via pip or apt-get
* Run `python app.py`
## Client part:
* Download the Android Studio project (LymeDetectorDesign)
* In ProcessingActivity.java include the IP adress or your server, port and "/predict"
* Build an APK
* Install on the smartphone


## How to retrain

* Download the whole project
* Put your data into the `processed_dataset` folder
* Run `learn_model_final.ipynb` or your own learning python file
* Move the saved .h5 Keras model into the server folder
* Change the name in the server `app.py` file to your .h5 file name
* Deploy
