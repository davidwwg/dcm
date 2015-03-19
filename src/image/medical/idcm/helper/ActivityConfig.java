package image.medical.idcm.helper;

import image.medical.idcm.R;
import image.medical.idcm.app.IDCMApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class ActivityConfig {

    private static ActivityConfig      activityConfig;
    private Map<String, ActivityModel> mActivityModelMap = new HashMap<String, ActivityModel>();

    private ActivityModel              mModel;

    public ActivityConfig() {
        try {
            InputStream is = IDCMApplication.getInstance().getResources().openRawResource(R.raw.activities_config);
            saxXMLFragments(is);
            mModel = null;
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ActivityConfig getInstance() {

        if (activityConfig == null) {
            activityConfig = new ActivityConfig();
        }
        return activityConfig;
    }

    public ActivityModel getActivityModel(String id) {
        return mActivityModelMap.get(id);
    }

    private void saxXMLFragments(InputStream is) {
        String rootTag = "Activities";
        String childTag = "Activity";
        RootElement root = new RootElement(rootTag);
        Element child = root.getChild(childTag);

        child.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                String id = attributes.getValue("id");
                String aName = attributes.getValue("name");
                mModel = new ActivityModel(id);
                mModel.className = aName;
            }
        });

        child.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                mActivityModelMap.put(mModel.id, mModel);
            }
        });

        try {
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public String getActivityCode(Class<?> clazz) {
        String code = "";
        Iterator<Entry<String, ActivityModel>> iter = mActivityModelMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ActivityModel> entry = (Map.Entry<String, ActivityModel>) iter.next();

            ActivityModel model = entry.getValue();
            if (model.className.equals(clazz.getName())) {
                code = entry.getKey();
                break;
            }

        }

        return code;
    }

}
