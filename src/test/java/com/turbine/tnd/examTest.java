package com.turbine.tnd;

/**
 * @author Turbine
 * @Description
 * @date 2022/9/9 15:11
 */
import java.util.Scanner;
public class examTest {

    static void Monkey_goto(int x,int y){
        int i = 0;
        i=i+1;
        System.out.println("step:"+ i +"Monkey从"+ x +"走到"+y);
    }

    static void Monkey_pushbox(int x,int y){
        int i=0;
        i=i+1;
        System.out.println("step:"+i+"Monkey把箱子从"+x+"推到"+y);
    }

    static void Monkey_climbon() {
        int i=0;
        i=i+1;
        System.out.println("step:"+i+"Monkey爬上箱子");
    }


    static  void Monkey_grasp(){
        int i=0;
        i=i+1;
        System.out.println("step:"+i+"Monkey摘到香蕉");
    }

    public static void main(String[] args) {
           /* for(int banana = 6; ; banana+=5){
                // temp记录每只猴子分完，剩余香蕉个数
                int temp = banana;
                // 第一只猴子
                if(temp%5==1){
                    temp = (temp-1)/5*4;
                }else{    // 不满足条件temp%5==1，说明香蕉个数不对，本次循环结束，开始下一次循环
                    continue;
                }
                // 第二只猴子
                if(temp%5==2){
                    temp = (temp-2)/5*4;
                }else{
                    continue;
                }
                // 第三只猴子
                if(temp%5==3){
                    temp = (temp-3)/5*4;
                }else{
                    continue;
                }
                // 第四只猴子
                if(temp%5==4){
                    temp = (temp-4)/5*4;
                }else{
                    continue;
                }
                // 第五只猴子
                if(temp>0 && temp%5==0){    // 注意：最后一只猴子醒来，还有香蕉，所以temp>0
                    System.out.println(banana);
                    break;
                }
            }*/


    }


}
