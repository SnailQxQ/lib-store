package com.turbine.tnd.bean;

import lombok.Data;
import lombok.ToString;

/**
 * @author Turbine
 * @Description
 * @date 2022/1/19 15:02
 */
@Data
@ToString
public class User {
   private int id;
   private String userName;
   private String password;
   private String sequence;


   @Override
   public String toString() {
      return "User{" +
              "id=" + id +
              ", userName='" + userName + '\'' +
              ", password='" + password + '\'' +
              ", sequence='" + sequence + '\'' +
              '}';


   }

   public static void main(String[] args) {
      int[][] arr = {{1,2,3},{1,2,3},{1,2,3}};

   }

}
