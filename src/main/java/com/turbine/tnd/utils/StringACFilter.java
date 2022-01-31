package com.turbine.tnd.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * @author Trubine
 * @Description Tire 树升级 AC自动机
 * @date 2022/1/23 18:20
 */
//字符串过滤器，过滤敏感词
//AC自动机
//当前模式串后缀和fail指针指向的模式串部分前缀相同
public class StringACFilter implements  Filter<String>{
    private static final String ignore = " &-=_";
    //根节点fail 是null
    private static Node ROOT = new Node();
    private static Node parent = ROOT;
    
    //private StringACFilter(){};


    static {

       String[] lexicon = {"ash","shex","bcd","sha"};
       initTree(lexicon);
    }


    //插入字符串到Tire
    private static void insert(String word){
      char[] chars = word.toCharArray();

      Node node = ROOT;

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
        initFailPoint();
    }
    //初始化fail，指针，层序遍历，第一层都是指向根
    // 关键步骤就是找当前节点的父节点的fail节点 FP， 看FP 有没有和当前节点值相同的有就指向他，没有就指向根节点。
    // 画图就能清晰的了解到这样操作就实现了，当前模式串后缀和fail指针指向的模式串部分前缀相同
    private static void initFailPoint(){
        dfs(ROOT);
        System.out.println(
                "============="
        );
    }
    
    //使用递归实现，这样找父节点方便点\
    //是在当前层将孩子节点的fail 指针都指向完毕
    private static void dfs(Node node){
        
        Map<Character, Node> children = node.children;
        if( !children.isEmpty() ){
            for(Node child : children.values()){
                if(node == ROOT){
                    child.fail = ROOT;
                }else {
                    if(node.fail != null){
                        child.fail = node.fail.children.getOrDefault(child.content,ROOT);
                    }
                }
                dfs(child);
            }
        }

    }
    
    

    //过滤字符串
    @Override
    public String filtration(String content) {
       char[] chars = content.toCharArray();
       int n = chars.length;
       StringBuilder builder = new StringBuilder();
       //i 是不会进行回退的， 模板树节点也不会回退,每次只添加上匹配不到的内容就行
        Node node = ROOT;
        int i=0;
        while(i < n){
            node = ROOT;
            StringBuilder temp = new StringBuilder();
            int fail=0;
            while(i < n && node != null){

                if(ignore.contains(chars[i]+"")){
                    temp.append(chars[i++]);
                    continue;
                }
                //当前node 匹配的是i-1
                if(node.isEnd){
                    //匹配成功就要把开头没有匹配上的n个加入答案
                    String s = temp.toString();
                    if(fail != 0)builder.append(s.substring(0,fail));
                    builder.append("#");
                    //添加完后清空temp
                    temp = temp.delete(0,temp.length());
                    node = ROOT;//匹配上了重置状态
                }
                if(node.children.containsKey(chars[i])){
                    node = node.children.get(chars[i]);
                }else{
                    //记录失败的次数很关键，失败次数意味着开头的 fail 个字符是不匹配的
                    node = node.fail;
                    //刚匹配完一次失败转向不计算
                    if(temp.length() != 0)fail++;
                }
                //保存可能需要保留的那些串
                temp.append(chars[i]);
                i++;
            }

            if(node != null && node.isEnd) builder.append("#");
            else builder.append(temp.toString());
        }

       return builder.toString();
    }




    private static class Node{
      char content;
      Map<Character,Node> children = new HashMap<>();
      boolean isEnd;
      //fail 指针，当匹配失败的时候就网fail指针去匹配
      //初始根节点 都为空，表示指向根节点
      Node fail;
    }


     public static void main(String[] args) {
         StringACFilter ff = new StringACFilter();
         //String content = "ashexshexsha";//预估*ex**
         String content = "---a- -sh =ex===shexsha";//预估*ex**
         System.out.println(ff.filtration(content));

     }
}
