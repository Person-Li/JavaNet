package com.lz.Jnet.Factory;

import com.lz.Jnet.Annot.Component;
import com.lz.Jnet.Annot.JAutowired;
import com.lz.Jnet.Annot.RequestRoute;
import com.lz.Jnet.Annot.RouterController;
import com.lz.Jnet.Util.Out;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CFactory {
    private final ConcurrentHashMap<String, Object> components=new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Method> getHandlerMethod = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Method> postHandlerMethod = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> cross = new ConcurrentHashMap<>();

    private static CFactory cf=null;
    public static CFactory getInstance(){
        if(cf==null)cf=new CFactory();
        return cf;
    }
    protected void init(String packgeName){
        Out.outBlue("开始扫描注解");
        Reflections reflections=null;
        reflections=new Reflections(new ConfigurationBuilder()
                .forPackages(packgeName) // 指定路径URL
                .addScanners(new SubTypesScanner()) // 添加子类扫描工具
                .addScanners(new FieldAnnotationsScanner()) // 添加 属性注解扫描工具
                .addScanners(new MethodAnnotationsScanner() ) // 添加 方法注解扫描工具
                .addScanners(new MethodParameterScanner() ) // 添加方法参数扫描工具
        );
        try {
            loadComponent(reflections);             //加载托管类
            injection(reflections);                 //执行属性注入
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
    void loadComponent(Reflections reflections) throws IllegalAccessException, InstantiationException {
        Set<Class<?>> set= reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> c:set){
            if(c.isAnnotation()) continue;
            if(!c.getAnnotation(Component.class).isLazy()) {
                components.put(c.getName(), c.newInstance());                          //直接装载组件
            }else {
                components.put(c.getName(),c);
            }
            RouterController router=c.getDeclaredAnnotation(RouterController.class);
            Method[] methods = c.getDeclaredMethods();
            if(router!=null){
                for (Method m : methods) {
                    RequestRoute requestRoute = m.getDeclaredAnnotation(RequestRoute.class);
                    if  (requestRoute==null){

                    }else
                    if (requestRoute.method().equals("GET")){
                        getHandlerMethod.put(router.url() + requestRoute.url(), m);
                    } else
                    if (requestRoute.method().equals("POST")){
                        postHandlerMethod.put(router.url() + requestRoute.url(), m);
                        if(router.isCross()){
                            cross.put(router.url() + requestRoute.url(),0);
                        }
                    }
                }
            }
        }
        Out.outBlue("扫描完成，共有:"+(getHandlerMethod.size()+postHandlerMethod.size())+"个接口");
    }
    void injection(Reflections reflections) throws IllegalAccessException {
        Set<Field> set= reflections.getFieldsAnnotatedWith(JAutowired.class);
        Out.outBlue("扫描到"+set.size()+"个可注入属性");
        short successNum=0;
        for(Field f:set){
            Object c=components.get(f.getDeclaringClass().getName());
            if(c!=null) {
                Object cc=components.get(f.getType().getName());
                if(cc!=null) {
                    f.setAccessible(true);
                    f.set(c, cc);
                    successNum++;
                }else {
                    Out.outErr("位于"+f.getDeclaringClass().getName()+"类中的属性\""+f.getName()+"\"注入失败\n原因:"+f.getType().getName()+"类未进行托管");
                }
            }
//            else
//            if(f.getDeclaringClass().getAnnotation(EkTest.class)!=null){
//                continue;
//            }else {
//                OutUtil.outErr(f.getDeclaringClass().getName()+"类未进行托管,无法执行自动注入操作");
//            }
        }
        Out.outBlue("属性注入完成,成功注入"+successNum+"个属性");
    }

}
