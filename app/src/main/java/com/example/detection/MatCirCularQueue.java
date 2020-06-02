package com.example.detection;

import org.opencv.core.Mat;

public class MatCirCularQueue {

    final int ArraySize = 200;
    int front = 0, rear = 0;
    Mat[] arr = new Mat[ArraySize];

    public void Enqueue(Mat data) {
        if ((rear + 1) % ArraySize == front % ArraySize) {
            // full
            this.Dequeue();
        }
        rear = (rear + 1) % ArraySize;
        arr[rear] = data;

    }

    public Mat Dequeue() {
        if (front == rear) {
            //empty
            return null;
        } else {
            front = (front + 1) % ArraySize;
            return arr[front];
        }
    }
}
