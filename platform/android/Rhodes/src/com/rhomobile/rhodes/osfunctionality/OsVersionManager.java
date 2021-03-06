package com.rhomobile.rhodes.osfunctionality;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import com.rhomobile.rhodes.Logger;

import android.os.Build;

public class OsVersionManager {
    private static final String TAG = OsVersionManager.class.getSimpleName();
 
    private static OsVersionManager sInstance;
    
    private static OsVersionManager getInstance() {
        if (sInstance == null) {
            sInstance = new OsVersionManager();
        }
        return sInstance;
    }
    
    private OsVersionManager() {
        mActiveFeatures = new HashMap<Class<?>, Object>();
        mSelectors = new ArrayList<HashMap<Class<?>, String>>(Build.VERSION.SDK_INT);
        for(int i = 0; i < Build.VERSION.SDK_INT; ++i) {
            mSelectors.add(new HashMap<Class<?>, String>());
        }
    }
    
    private HashMap<Class<?>, String> selectFeatureMap(int version) {
        Logger.T(TAG, "Select feature map for OS ver. " + version + ", idx: " + (version-1));
        if (version > mSelectors.size()) {
            Logger.W(TAG, "App is built with SDK ver. " + mSelectors.size());
            version = mSelectors.size();
        }
        return mSelectors.get(version - 1);
    }
    
//    private static class Selector<SelectorClass extends Object> {
//        private SelectorClass mSelector;
//        private Class<Object> mClass;
//        Selector(SelectorClass selector, Class<Object> selectorClass) {
//            mSelector = selector;
//            mClass = selectorClass;
//        }
//        Selector(){}
//        SelectorClass getSelector() { return mSelector; }
//        Class<Object> getSelectorClass() { return mClass; }
//    }
    
    private HashMap<Class<?>, Object> mActiveFeatures;
    private ArrayList<HashMap<Class<?>, String>> mSelectors;

    private Object getFeature1(Class<?> selectorIface) {
        Logger.T(TAG, "Looking " + selectorIface);
        return mActiveFeatures.get(selectorIface);
    }
    
    private void setSelector(int version, Class<?> featureIface, String selectorClassName) {
        Logger.T(TAG, "Set " + featureIface + "->" +selectorClassName + " for OS rev. " + version);

        HashMap<Class<?>, String> theVersion = selectFeatureMap(version);
        theVersion.put(featureIface, selectorClassName);

        for (int i = version + 1; i <= Build.VERSION.SDK_INT; ++i) {
            Logger.T(TAG, "Check feature for OS rev. " + i);
            HashMap<Class<?>, String> curVersion = selectFeatureMap(i);
            //if (!curVersion.containsKey(featureIface)) {
                Logger.T(TAG, "OS " + i + ": " + featureIface + "->" + selectorClassName);
                curVersion.put(featureIface, selectorClassName);
            //} else {
            //    break;
            //}
        }
        
        refreshFeature(featureIface);
    }
    
    private void refreshFeature(Class<?> featureIface) {
        Logger.T(TAG, "refreshFeature: " + featureIface);
        String selectorClassName = selectFeatureMap(Build.VERSION.SDK_INT).get(featureIface);
        try {
            Class<?> selectorClass = Class.forName(selectorClassName).asSubclass(featureIface);
            
            Logger.T(TAG, "Set " + featureIface + "->" + selectorClass);
            
            Constructor<?> ctor = selectorClass.getConstructor();
            Object feature = ctor.newInstance();
            
            //Object feature = selectorClass.newInstance();
            
            if (feature == null) {
                Logger.E(TAG, "Cannot create feature instance!");
            } else {
                mActiveFeatures.put(featureIface, feature);
            }
        } catch (Throwable e) {
            Logger.E(TAG, e);
        }
        
    }

    public static void registerSelector(int version, Class<?> featureIface, String selectorClassName) {
        OsVersionManager instance = getInstance();
//        class TheSelector extends Selector<SelectorIface> {
//            TheSelector(SelectorIface selector) {
//                super(selector, (Class<Object>)((ParameterizedType)TheSelector.class.getGenericSuperclass()).getActualTypeArguments()[0]);
//            }
//        }
//        Type selectorType = TheSelector.class.getGenericSuperclass();
//        TypeVariable genType = selectorType.getClass().getTypeParameters()[0];
        
        instance.setSelector(version, featureIface, selectorClassName);
    }
    
    public static void registerSelector(Class<?> featureIface, String selectorClassName) {
        OsVersionManager instance = getInstance();
        instance.setSelector(1, featureIface, selectorClassName);
    }

    public static <SelectorClass> SelectorClass getFeature(Class<?> featureIface) {
        OsVersionManager instance = getInstance();
        
        Object selectorObject = instance.getFeature1(featureIface);
        return (SelectorClass)selectorObject;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
}
