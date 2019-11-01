package com.example.forwalk;

public class ArrayQueue {

    // 큐 배열은 front와 rear 그리고 maxSize를 가진다.
    private int front;
    private int rear;
    private int maxSize;
    private Object[] queueArray;

    // 큐 배열 생성
    public ArrayQueue(int maxSize){

        this.front = 0;
        this.rear = -1;
        this.maxSize = maxSize;
        this.queueArray = new Object[maxSize];
    }

    // 큐가 비어있는지 확인
    public boolean empty(){
        return (front == rear+1);
    }

    // 큐가 꽉 찼는지 확인
    public boolean full(){
        return (rear == maxSize-1);
    }

    // 큐 rear에 데이터 등록
    public void insert(Object item){

        if(full()) throw new ArrayIndexOutOfBoundsException();

        queueArray[++rear] = item;
    }

    // 큐에서 front 데이터 조회
    public Object peek(){

        if(empty()) throw new ArrayIndexOutOfBoundsException();

        return queueArray[front];
    }

    // 큐에서 front 데이터 제거
    public Object remove(){

        Object item = peek();
        front++;
        return item;
    }

}