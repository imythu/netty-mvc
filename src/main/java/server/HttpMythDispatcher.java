package server;

import annotation.*;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class HttpMythDispatcher {
    private HttpMythResponse response;
    private HttpMythRequest request;

    private static List<String> classNames = new ArrayList<>();
    private static Map<String, Object> ioc = new HashMap<>();
    private static Map<String, Method> handlerMapping = new HashMap<>();

    public HttpMythDispatcher(HttpMythRequest request, HttpMythResponse response) {
        this.request = request;
        this.response = response;
    }

    public static void  runDispatcher() throws Exception {
        System.out.println("开始扫描");
        doScanner("mvc");
        System.out.println("扫描Controller完毕");
        for (String name : classNames) System.out.println("扫描到类名: "+name);


        System.out.println("创建实例");
        doInstance();
        for (String key:ioc.keySet()) System.out.println("创建实例："+key+"--"+ioc.get(key));
        System.out.println("实例创建完毕");


        System.out.println("自动注入");
        doAutowired();
        System.out.println("自动注入完毕");

        System.out.println("初始化mapping");
        initHandlerMapping();
        System.out.println("mapping有以下链接：");
        for (String key:handlerMapping.keySet()) {
            System.out.println(key);
        }
        System.out.println("初始化mapping完毕");
    }

    private static void doScanner(String packageName) throws Exception {
        String url = HttpMythDispatcher.class.getResource("/")+packageName.replaceAll("\\.", "/");
        url = url.substring(6);
        File dir = new File(url);
        File[] files = dir.listFiles();
        if (files == null) return;
        System.out.println("扫描长度: "+files.length);
        for (File file : files) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                String className = packageName + "." + file.getName().replace(".class", "").trim();
                int position = classNames.indexOf(className);
                if (position >= 0) {
                    classNames.set(position, className);
                }else {
                    classNames.add(className);
                }
            }
        }
    }

//    private void doConfig(String location) {
//        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(location);
//
//    }
    private static String lowerFirstCase(String str) throws Exception {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private static void doInstance() throws Exception {
        if (classNames.size() == 0) return;
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MythController.class)) {
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MythRestController.class)) {
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(MythService.class)) {
                    MythService mythService = clazz.getAnnotation(MythService.class);
                    String beanName = mythService.value().trim();
                    if (!"".equals(beanName)) {
                        ioc.put(beanName, clazz.newInstance());
                        continue;
                    }
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(lowerFirstCase(i.getSimpleName()), clazz.newInstance());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doAutowired() throws Exception {
        if (ioc.size() == 0) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(MythAutowired.class)) continue;
                MythAutowired autowired = field.getAnnotation(MythAutowired.class);
                String beanName = autowired.value().trim();
                if (beanName.equals("")) beanName = lowerFirstCase(field.getType().getInterfaces()[0].getSimpleName());
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                    System.out.println("注入的beanName："+ beanName);
//                    System.out.println(entry.getValue().toString() + ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private static void initHandlerMapping() throws Exception {
        if (ioc.size() == 0) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            String baseurl;
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(MythController.class)
                    && !clazz.isAnnotationPresent(MythRestController.class)) continue;
            baseurl = "";
            if (clazz.isAnnotationPresent(MythRequestMapping.class)) {
                MythRequestMapping requestMapping = clazz.getAnnotation(MythRequestMapping.class);
                baseurl = requestMapping.value();
            }
            System.out.print(clazz.getSimpleName()+ ":  ");
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(MythRequestMapping.class)) continue;
                MythRequestMapping requestMapping = method.getAnnotation(MythRequestMapping.class);
                String url = ("/" + baseurl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.print(method.getName()+"  ");
            }
            System.out.println();
        }
    }

    public int doDispatcher(HttpMythRequest request, HttpMythResponse response) {
        String url = request.getUrl();
        System.out.println("HttpMythDispathcer#doDispatcher: url是"+url);
        if (request.getUrl() == null || handlerMapping.size() == 0 ||!handlerMapping.containsKey(url)) {

            writeStatusCode(response, "404");
            return -1;
        }
        try {
            //Map<String, String> parametersMap = request.getParameterMap();
            Method method = handlerMapping.get(url);
            Class<?>[] parameterTypes = method.getParameterTypes();
            int length = parameterTypes.length;
            Object[] parameterValues = new Object[length];
            for (int i = 0;i < length;i++) {
                if (parameterTypes[i] == HttpMythRequest.class) {
                    parameterValues[i] = request;
                    continue;
                }
                if (parameterTypes[i] == HttpMythResponse.class) {
                    parameterValues[i] = response;
                    continue;
                }
            }
            String beanName = lowerFirstCase(method.getDeclaringClass().getSimpleName());
            Object object = method.invoke(ioc.get(beanName), parameterValues);
            if (method.getDeclaringClass().getAnnotation(MythRestController.class) != null) {
                String jsonData = new Gson().toJson(object);
                response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json; charset=utf-8");
                response.writeAndFlush(jsonData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    private void writeStatusCode(HttpMythResponse response, String statusCode) {
        response.write(statusCode);
    }
}