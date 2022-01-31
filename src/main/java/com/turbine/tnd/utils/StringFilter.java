package com.turbine.tnd.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Trubine
 * @Description Tire 树实现的过滤方式
 * @date 2022/1/23 18:20
 */
//字符串过滤器，过滤敏感词
//Tire 树
public class StringFilter implements  Filter<String>{

    private static Node root = new Node();

    private StringFilter(){};


    static {
       String[] lexicon = {"傻逼","sd","fqfw","我","sb"};
       initTree(lexicon);
    }


    //插入字符串到Tire
    private static void insert(String word){
      char[] chars = word.toCharArray();

      Node node = root;

      for(int i=0 ;i<chars.length ;i++){

        if( node.children.containsKey(chars[i]) ){
         //已有
            node = node.children.get(chars[i]);
        }else{
           Node child = new Node();
           child.content = chars[i];
           node.children.put(chars[i],child);

           node = child;
        }

      }

      node.isEnd = true;//单词结尾
    }

    //初始化Tire
    private static void initTree(String[] strs){
       for (int i=0 ;i<strs.length ;i++){
          insert(strs[i]);
       }
    }

    //过滤字符串
    @Override
    public String filtration(String content) {
       char[] chars = content.toCharArray();
       int n = chars.length;
       StringBuilder builder = new StringBuilder();
       //失配或者到达一个词的结尾就 进行相应处理，并且回溯
       for(int i=0 ;i<n ;i++){
        Node node = root;
            if( node.children.containsKey(chars[i]) ){
                int j = i;
              while( j<n &&  node.children.containsKey(chars[j]) && !node.isEnd ){
                   node = node.children.get(chars[j]);
                   j++;
              }

              if(node.isEnd){
                   builder.append("*");
                   i = j-1;
              }else{
                   //失配的情况
                   builder.append(chars[i]);
              }

            }else builder.append(chars[i]);
       }

       return builder.toString();
    }


    private static class Node{
      char content;
      Map<Character,Node> children = new HashMap<>();
      boolean isEnd;
    }


     public static void main(String[] args) {
         FilterFactor ff = new FilterFactor();
        String content = "sb傻逼sasdfqfw我sbasdqwwefffffaervggagraer";
         Filter<String> filter = ff.getResource(FilterFactor.filterOpt.GEN_FILTER);
         System.out.println(content);
         String result = filter.filtration(content);
         System.out.println(result);

     }
}
