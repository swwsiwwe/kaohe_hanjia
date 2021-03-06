package com.company.service;

import com.company.dao.IUserWorkDao;
import com.company.dao.MySqlUtils;
import com.company.domain.UserWork;
import java.util.List;

public class UserWorkService {

    public static void updateStatus(String status,String studentID,String work){
        MySqlUtils MySqlUtils = new MySqlUtils();
        MySqlUtils.init();
        IUserWorkDao iUserWorkDao = MySqlUtils.getIUserWorkDao();
        iUserWorkDao.updateStatus(status,studentID,work);
        MySqlUtils.commit();
        MySqlUtils.close();
    }

    public static List<UserWork> deleteInvalidWork(String studentID){
        MySqlUtils MySqlUtils = new MySqlUtils();
        MySqlUtils.init();
        IUserWorkDao iUserWorkDao = MySqlUtils.getIUserWorkDao();
        List<UserWork> list = iUserWorkDao.selectInvalidUserWork(studentID,"已删除");
        iUserWorkDao.deleteAllByStatus(studentID,"已删除");
        MySqlUtils.commit();
        MySqlUtils.close();
        if(list!=null&&!list.isEmpty()){
            return list;
        }else{
            return null;
        }
    }

    public static void deleteWorks(String studentID,List<String> works){
        MySqlUtils MySqlUtils = new MySqlUtils();
        MySqlUtils.init();
        IUserWorkDao iUserWorkDao = MySqlUtils.getIUserWorkDao();
        if(works!=null)
        for(String work:works){
            iUserWorkDao.delete(work,studentID);
        }
        MySqlUtils.commit();
        MySqlUtils.close();
    }

    public static void deleteWork(String status,String work){
        MySqlUtils MySqlUtils = new MySqlUtils();
        MySqlUtils.init();
        IUserWorkDao iUserWorkDao = MySqlUtils.getIUserWorkDao();
        iUserWorkDao.delete_Status(status,work);
        MySqlUtils.commit();
        MySqlUtils.close();
    }
}