/*
 * test.cpp
 *
 *  Created on: Nov 21, 2012
 *      Author: shaofenchen
 */

#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
using namespace std;
using namespace cv;

#define NUM_FRAME 100 //只处理前300帧，根据视频帧数可修改
void Video_to_image(char* filename) {
	printf("------------- video to image ... ----------------\n");
	//初始化一个视频文件捕捉器
	CvCapture* capture = cvCaptureFromAVI(filename);
	//获取视频信息
	cvQueryFrame(capture);
	int frameH = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT);
	int frameW = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
	int fps = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FPS);
	int numFrames =
			(int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_COUNT);
	printf(
			"\tvideo height : %d\n\tvideo width : %d\n\tfps : %d\n\tframe numbers : %d\n",
			frameH, frameW, fps, numFrames);
	//定义和初始化变量
	int i = 0;
	IplImage* img = 0;
	char image_name[13];

	//cvNamedWindow( "mainWin", CV_WINDOW_AUTOSIZE );
	//读取和显示
	while (1) {

		img = cvQueryFrame(capture); //获取一帧图片
		//cvShowImage( "mainWin", img ); //将其显示
		char key = cvWaitKey(2000);

		sprintf(image_name, "%s%d%s", "image", ++i, ".jpg");//保存的图片名

		cvSaveImage(image_name, img); //保存一帧图片

		if (i == NUM_FRAME)
			break;
	}
	cvReleaseCapture(&capture);
	//cvDestroyWindow("mainWin");
}

void Image_to_video() {
	int i = 0;
	IplImage* img = 0;
	char image_name[13];
	printf("------------- image to video ... ----------------\n");
	//初始化视频编写器，参数根据实际视频文件修改
	CvVideoWriter *writer = 0;
	int isColor = 1;
	int fps = 30; // or 25
	int frameW = 400; // 744 for firewire cameras
	int frameH = 240; // 480 for firewire cameras
	writer = cvCreateVideoWriter("out.avi", CV_FOURCC('X', 'V', 'I', 'D'), fps,
			cvSize(frameW, frameH), isColor);
	printf("\tvideo height : %d\n\tvideo width : %d\n\tfps : %d\n", frameH,
			frameW, fps);
	//创建窗口
	cvNamedWindow("mainWin", CV_WINDOW_AUTOSIZE);
	while (i < NUM_FRAME) {
		sprintf(image_name, "%s%d%s", "image", ++i, ".jpg");
		img = cvLoadImage(image_name);
		if (!img) {
			printf("Could not load image file...\n");
			exit(0);
		}
		cvShowImage("mainWin", img);
		char key = cvWaitKey(20);
		cvWriteFrame(writer, img);
	}
	cvReleaseVideoWriter(&writer);
	cvDestroyWindow("mainWin");
}

//int main(int argc, char *argv[]) {
//	char filename[13] = "Mission.avi";
//	Video_to_image(filename); //视频转图片
//	Image_to_video(); //图片转视频
//	return 0;
//}

/** Function Headers */
void detectAndDisplay(Mat frame);

/** Global variables */
String face_cascade_name = "haarcascade_frontalface_alt.xml";
String eyes_cascade_name = "haarcascade_eye_tree_eyeglasses.xml";
CascadeClassifier face_cascade;
CascadeClassifier eyes_cascade;
string window_name = "Capture - Face detection";
RNG rng(12345);

/** @function main */
int main(int argc, const char** argv) {
	CvCapture* capture;
	Mat frame;
	char filename[13] = "Mission.avi";
	//-- 1. Load the cascades
	if (!face_cascade.load(face_cascade_name)) {
		printf("--(!)Error loading\n");
		return -1;
	};
	if (!eyes_cascade.load(eyes_cascade_name)) {
		printf("--(!)Error loading\n");
		return -1;
	};

	//++++++++++++++++++++++
	printf("------------- video to image ... ----------------\n");
	//初始化一个视频文件捕捉器
	capture = cvCaptureFromAVI(filename);
	//获取视频信息
	cvQueryFrame(capture);
	int frameH = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT);
	int frameW = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
	int fps = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FPS);
	int numFrames =
			(int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_COUNT);
	printf(
			"\tvideo height : %d\n\tvideo width : %d\n\tfps : %d\n\tframe numbers : %d\n",
			frameH, frameW, fps, numFrames);
	//定义和初始化变量
	int i = 0;
	IplImage* img = 0;
	char image_name[13];

	//cvNamedWindow( "mainWin", CV_WINDOW_AUTOSIZE );
	//读取和显示
	while (1) {

		//img = cvQueryFrame(capture); //获取一帧图片
		//cvShowImage( "mainWin", img ); //将其显示
		// char key = cvWaitKey(20);

		//sprintf(image_name, "%s%d%s", "image", ++i, ".jpg");//保存的图片名

		//cvSaveImage( image_name, img);   //保存一帧图片
		frame = cvQueryFrame(capture);

		//-- 3. Apply the classifier to the frame
		if (!frame.empty()) {
			detectAndDisplay(frame);
		} else {
			printf(" --(!) No captured frame -- Break!");
			break;
		}

		int c = waitKey(10);
		if ((char) c == 'c') {
			break;
		}

		if (i == NUM_FRAME)
			break;
	}
	cvReleaseCapture(&capture);
	//+++++++++++++++++++++++++
	//-- 2. Read the video stream
//	// capture = cvCaptureFromCAM( -1 );
//	if (capture) {
//		while (true) {
//			frame = cvQueryFrame(capture);
//
//			//-- 3. Apply the classifier to the frame
//			if (!frame.empty()) {
//				detectAndDisplay(frame);
//			} else {
//				printf(" --(!) No captured frame -- Break!");
//				break;
//			}
//
//			int c = waitKey(10);
//			if ((char) c == 'c') {
//				break;
//			}
//		}
//	} else
//		printf("nothing happen");
	return 0;
}

/** @function detectAndDisplay */
void detectAndDisplay(Mat frame) {
	std::vector<Rect> faces;
	Mat frame_gray;

	cvtColor(frame, frame_gray, CV_BGR2GRAY);
	equalizeHist(frame_gray, frame_gray);

	//-- Detect faces
	face_cascade.detectMultiScale(frame_gray, faces, 1.1, 2, 0
			| CV_HAAR_SCALE_IMAGE, Size(30, 30));

	for (int i = 0; i < faces.size(); i++) {
		Point center(faces[i].x + faces[i].width * 0.5, faces[i].y
				+ faces[i].height * 0.5);
		ellipse(frame, center,
				Size(faces[i].width * 0.5, faces[i].height * 0.5), 0, 0, 360,
				Scalar(255, 0, 255), 4, 8, 0);

		Mat faceROI = frame_gray(faces[i]);
		std::vector<Rect> eyes;

		//-- In each face, detect eyes
		eyes_cascade.detectMultiScale(faceROI, eyes, 1.1, 2, 0
				| CV_HAAR_SCALE_IMAGE, Size(30, 30));

		for (int j = 0; j < eyes.size(); j++) {
			Point center(faces[i].x + eyes[j].x + eyes[j].width * 0.5,
					faces[i].y + eyes[j].y + eyes[j].height * 0.5);
			int radius = cvRound((eyes[j].width + eyes[j].height) * 0.25);
			circle(frame, center, radius, Scalar(255, 0, 0), 4, 8, 0);
		}
	}
	//-- Show what you got
	imshow(window_name, frame);
}
