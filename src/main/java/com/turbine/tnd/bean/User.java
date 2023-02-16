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
   private Integer id;
   private String userName;
   private String password;
   private String sequence;
   private Object profile;
   private String intro;



   public boolean isValid(){
      return userName != null && password != null;
   }

   @Override
   public String toString() {
      return "User{" +
              "id=" + id +
              ", userName='" + userName + '\'' +
              ", password='" + password + '\'' +
              ", sequence='" + sequence + '\'' +
              '}';


   }


}
