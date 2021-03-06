package com.company.controller;

import com.alibaba.fastjson.JSONObject;
import com.company.domain.*;
import com.company.service.*;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 用户页面交互
 */
@Controller
@RequestMapping("/static")
public class TeacherController {
    /**
     * 教师查询考核信息/考核管理
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/selectWorks")
    public void selectWorks(HttpServletRequest request, HttpServletResponse response) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            JSONObject js = JsonService.createJson(true);
            JsonService.writeArrayWorkJson(js, WorkService.findWorkById(t.getWorkID()));
            JsonService.write(response, js);
        }
    }

    /**
     * 教师发布考核
     * @param name
     * @param type
     * @param level
     * @param end
     * @param upload
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/newWork")
    public void newWork(String name, String type, Integer level, String end,@RequestParam("sfile") MultipartFile upload, HttpServletRequest request, HttpServletResponse response) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        Work work = new Work();
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            work.setType(type);
            work.setLevel(level);
            work.setName(name);
            work.setEnd(Work.toDate(end));
            work.setWorkID(t.getWorkID());
            work.setStart(new Date());
            if(upload==null){
                JSONObject js = JsonService.createJson(false);
                JsonService.putJson(js, "error", "未检测到文件，请上传考核文件(.txt)");
                JsonService.write(response, js);
            }else{
                /*获取文件名*/
               String filename = upload.getOriginalFilename();
                String str = filename.substring(filename.lastIndexOf("."));
                /*检测后缀*/
                if (".txt".equals(str)) {
                    int id = WorkService.insertWork(work);
                    filename = type + "" + level + "" + id + ".txt";
                    try {
                        FileService.upload(request, upload, "/works/", filename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JSONObject js = JsonService.createJson(true);
                    JsonService.putJson(js,"work",type + "" + level + "" + id);
                    JsonService.write(response, js);
                } else {
                    JSONObject js = JsonService.createJson(false);
                    JsonService.putJson(js, "error", "请上传“.txt”文件");
                    JsonService.write(response, js);
                }
            }

        }
    }
    /**
     * 教师个人信息展示
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/findTeacher")
    public void findTeacher(HttpServletRequest request, HttpServletResponse response) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            JSONObject js = JsonService.createJson(true);
            JsonService.writeTeacherJson(js, t);
            JsonService.write(response, js);
            LoginService.saveLogin(request, response, t, Key.TEACHER);
        }
    }

    /**
     * 修改教师信息
     * @param json
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/update")
    public void updateTeacher(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        Map<String,String> map = (Map<String, String>) JsonService.getJson(json);
        String name = map.get("name");
        String workID = map.get("workID");
        String tel = map.get("tel");
        String type =map.get("type");
        if (t == null || t.getWorkID() == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else if (!t.getWorkID().equals(workID)) {
            LoginService.saveLogin(request, response, t, Key.USER);
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "工号不允许修改");
            JsonService.write(response, js);
        } else {
            String tp = t.getWorkID();
            TeacherService.updateName(name, tp);
            TeacherService.updateTel(tel, tp);
            TeacherService.updateType(type, tp);
            Teacher teacher1 = TeacherService.findByWorkID(tp);
            LoginService.saveLogin(request, response, teacher1, Key.TEACHER);
            JSONObject js = JsonService.createJson(true);
            JsonService.write(response, js);
        }
    }

    /**
     * 教师修改密码
     * @param s
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/updatePassword")
    public void updateUserPassword(@RequestBody String s, HttpServletRequest request, HttpServletResponse response) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        System.out.println(t);
        Map<String,String> map = (Map<String, String>) JsonService.getJson(s);
        System.out.println(map);
        String old_password = map.get("old_password");
        String new_password1 = map.get("new_password1");
        String new_password2 = map.get("new_password2");
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        }else if(new_password1==null||!new_password1.equals(new_password2)){
            LoginService.saveLogin(request, response, t, Key.USER);
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "确认密码和新密码必须保持一致");
            JsonService.write(response, js);
        }else if(!t.getPassword().equals(old_password)){
            LoginService.saveLogin(request, response, t, Key.USER);
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "密码错误");
            JsonService.write(response, js);
        }
        else if(!old_password.equals(new_password1)){
            TeacherService.updatePassword(new_password1, t.getWorkID());
            t.setPassword(new_password1);
            LoginService.saveLogin(request, response, t, Key.USER);
            JSONObject js = JsonService.createJson(true);
            JsonService.write(response, js);
        }
    }

    /**
     * 教师发布考核
     * @param request
     * @param response
     * @param work
     * @param name
     * @param end
     * @param upload
     */
    @RequestMapping("/teacher/updateWork")
    public void updateWork(HttpServletRequest request, HttpServletResponse response,@RequestParam("code") String work,String name, String end,@RequestParam("sfile") MultipartFile upload) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            WorkService.update(work,name,end);
            if(upload==null||upload.isEmpty()){
                JSONObject js = JsonService.createJson(false);
                JsonService.putJson(js, "error", "未检测到文件，请上传考核文件(.txt)");
                JsonService.write(response, js);
            }else{
                /*获取文件名*/
                String filename = upload.getOriginalFilename();
                String str = filename.substring(filename.lastIndexOf("."));
                /*检测后缀*/
                if (".txt".equals(str)) {
                    filename = work + ".txt";
                    try {
                        FileService.upload(request, upload, "/works/", filename);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JSONObject js = JsonService.createJson(true);
                    JsonService.write(response, js);
                } else {
                    JSONObject js = JsonService.createJson(false);
                    JsonService.putJson(js, "error", "请上传“.txt”压缩文件");
                    JsonService.write(response, js);
                }
            }
        }
    }

    /**
     * 教师查看提交
     * @param request
     * @param response
     * @param json
     */
    @RequestMapping("/teacher/judgeWork")
    public void judgeWork(HttpServletRequest request, HttpServletResponse response,@RequestBody String json) {
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        Map<String,String> map = (Map<String, String>) JsonService.getJson(json);
        System.out.println(map);
        String work = map.get("code");
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            List<UserWork> list = TeacherService.getUserWorks(work);
            JSONObject js = JsonService.createJson(true);
            JsonService.writeTeacherWorkJson(js, list);
            JsonService.write(response, js);
        }
    }

    /**
     * 教师作业批改
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/judge")
    public void judge(HttpServletRequest request, HttpServletResponse response,@RequestBody String json) {
        System.out.println(json);
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        Map<String,String> map = (Map<String, String>) JsonService.getJson(json);
        String status = map.get("status");
        String studentID =map.get("studentID");
        String work = map.get("code");
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            UserWorkService.updateStatus(status, studentID,work);
            JSONObject js = JsonService.createJson(true);
            JsonService.write(response, js);
        }
    }

    /**
     * 教师删除考核
     * @param request
     * @param response
     */
    @RequestMapping("/teacher/deleteWork")
    public void deleteWork(@RequestBody String jsons,HttpServletRequest request, HttpServletResponse response) {
        System.out.println(jsons);
        Map<String,List<Map<String,String>>> map = (Map<String, List<Map<String, String>>>) JsonService.getJson(jsons);
        System.out.println(map);
        List<Map<String,String>> list = map.get("work");
        System.out.println(list);
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
            JsonService.write(response, js);
        } else {
            LoginService.saveLogin(request, response, t, Key.TEACHER);
            for(Map<String,String> work:list) {
                if (work != null) {
                    WorkService.delete(work.get("work"));
                    UserWorkService.deleteWork("已删除", work.get("work"));
                    FileService.workDelete(request, "/works/", work.get("work") + ".txt");
                }
            }
            JSONObject js = JsonService.createJson(true);
            JsonService.write(response, js);
        }
    }

    /**
     * 教师下载提交作业
     * @param request
     * @param
     * @param works
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/teacher/download")
    public  ResponseEntity<byte[]> UserDownload(HttpServletRequest request,@RequestBody String works) throws IOException {
        byte[] body = null;
        HttpHeaders headers = null;
        String path = request.getServletContext().getRealPath("/uploads/");
        System.out.println(works);
        Map<String, Object> map = (Map<String, Object>) JsonService.getJson(works);
        System.out.println(map.get("work"));
        List<Map<String, String>> list = (List<Map<String, String>>) map.get("studentID");
        String work = (String) map.get("work");
        if (list != null && !list.isEmpty()) {
            for (Map<String, String> studentID : list) {
                File file = new File(path + work + studentID.get("studentID") + ".zip");
                InputStream is = new FileInputStream(file);
                body = new byte[is.available()];
                is.read(body);
                headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment;filename=" + file.getName());
            }
        }
        System.out.println("null");
        ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(body, headers, HttpStatus.OK);
        return entity;
    }

    /**
     * 教师批量下载文件
     * @param request
     * @param works
     * @return 下载文件流
     * @throws IOException
     */
    @RequestMapping("/teacher/downloadAll")
    public ResponseEntity<byte[]> downloadAll(HttpServletRequest request,@RequestBody String works) throws IOException {
        File file;
        HttpHeaders headers;
        Map<String, Object> map = (Map<String, Object>) JsonService.getJson(works);
        System.out.println(map.get("work"));
        String work = (String) map.get("work");
        List<Map<String, String>> list = (List<Map<String, String>>) map.get("studentID");
        String resourcesName = "kao_he.zip";
        String path = request.getServletContext().getRealPath("/uploads/");
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(path + resourcesName));
        InputStream input = null;
        if (list != null && !list.isEmpty()) {
            for (Map<String, String> studentID : list) {
                String name = path + work + studentID.get("studentID") + ".zip";
                input = new FileInputStream(new File(name));
                zipOut.putNextEntry(new ZipEntry(work + studentID.get("studentID") + ".zip"));
                int temp = 0;
                while ((temp = input.read()) != -1) {
                    zipOut.write(temp);
                }
            }
        }
        input.close();
        zipOut.close();
        file = new File(path + resourcesName);
        headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + file.getName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.OK);
    }

    /**
     * 教师查看考核详情
     * @param request
     * @param response
     * @param json
     */
    @RequestMapping("/teacher/getWork")
    public void getWork(HttpServletRequest request, HttpServletResponse response,@RequestBody String json) {
        Map<String,String> map = (Map<String, String>) JsonService.getJson(json);
        String work = map.get("code");
        Teacher t = (Teacher) LoginService.getLogin(request, Key.TEACHER);
        if (t == null) {
            JSONObject js = JsonService.createJson(false);
            JsonService.putJson(js, "error", "timeOut");
        } else {
            Work w = WorkService.findWorkByCode(work);
            JSONObject js = JsonService.createJson(true);
            JsonService.writeWorkJson(js, w);
            JsonService.write(response, js);
        }
    }
}