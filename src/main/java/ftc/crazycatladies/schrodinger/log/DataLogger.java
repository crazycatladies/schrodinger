package ftc.crazycatladies.schrodinger.log;

import org.json.JSONException;
import org.json.JSONObject;

import ftc.crazycatladies.schrodinger.opmode.OpModeTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Date;

public class DataLogger {
    private static DataLogger logger;
    private BufferedWriter writer;
    private OpModeTime time;
    private final String run;

    private DataLogger(OpModeTime time, String opModeName) {
        this.time = time;
        run = "" + new Date().getTime();
        String filename = opModeName + "-" + run + ".log";

        try {
            File file = new File("sdcard", filename);
            writer = new BufferedWriter(new FileWriter(file));

            File listFile = new File("sdcard", "listing.log");
            BufferedWriter listWriter = new BufferedWriter(new FileWriter(listFile, true));
            listWriter.write(filename + "\n");
            listWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            writer = new BufferedWriter(new StringWriter());
        }

        JSONObject json = new JSONObject();
        putOpt(json, "opMode", opModeName);
        putOpt(json, "time", "" + new Date().getTime());
        log(json);

        File context = new File("/sdcard/FIRST", "context.json");
        try {
            FileInputStream fis = new FileInputStream(context);
            byte[] b = new byte[1024 * 64];
            fis.read(b);

            json = new JSONObject();
            json.put("context", new JSONObject(new String(b)));

            log(json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static DataLogger createDataLogger(OpModeTime opModeTime, String opModeName) {
        return logger = new DataLogger(opModeTime, opModeName);
    }

    public static JSONObject createJsonObject(String type, String name) {
        JSONObject json = new JSONObject();
        putOpt(json, "type", type);
        putOpt(json, "name", name);
        return json;
    }

    public void log(JSONObject json) {
       putOpt(json, "t", "" + String.format("%.3f", time.time()));
        String str = json.toString();

        try {
            writer.write(str);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DataLogger getLogger() {
        return logger;
    }

    public static void putOpt(JSONObject json, String name, Object value) {
        try {
            if (value instanceof Double) {
                json.putOpt(name, value.toString());
            } else
                json.putOpt(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
