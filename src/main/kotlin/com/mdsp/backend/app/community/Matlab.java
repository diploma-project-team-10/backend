package com.mdsp.backend.app.community;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabEngine;

import java.util.concurrent.ExecutionException;

public class Matlab {
    private static MatlabEngine matlabEngine;

    public static MatlabEngine getMatlabEngine() throws EngineException, InterruptedException {
        if(matlabEngine == null){
            matlabEngine = MatlabEngine.startMatlab();
        }
        return matlabEngine;
    }

    public static void eval(String s) throws ExecutionException, InterruptedException {
        matlabEngine.eval(s);
    }

    public static String getVariable(String s) throws ExecutionException, InterruptedException {
        return matlabEngine.getVariable(s);
    }

}
