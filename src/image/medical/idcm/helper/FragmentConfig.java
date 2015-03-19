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

public class FragmentConfig {
    private static FragmentConfig      fragmentConfig;
    private Map<String, FragmentModel> mFragmentModelMap = new HashMap<String, FragmentModel>();

    private FragmentModel              mModel;

    public FragmentConfig() {
        try {
            InputStream is = IDCMApplication.getInstance().getResources().openRawResource(R.raw.fragments_config);
            saxXMLFragments(is);
            mModel = null;
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FragmentConfig getInstance() {

        if (fragmentConfig == null) {
            fragmentConfig = new FragmentConfig();
        }
        return fragmentConfig;
    }

    public FragmentModel getFragmentModel(String id) {
        return mFragmentModelMap.get(id);
    }

    private void saxXMLFragments(InputStream is) {
        String rootTag = "Fragments";
        String childTag = "Fragment";
        RootElement root = new RootElement(rootTag);
        Element child = root.getChild(childTag);

        child.setStartElementListener(new StartElementListener() {

            @Override
            public void start(Attributes attributes) {
                String id = attributes.getValue("id");
                String aName = attributes.getValue("name");
                mModel = new FragmentModel(id);
                mModel.className = aName;
            }
        });

        child.setEndElementListener(new EndElementListener() {

            @Override
            public void end() {
                mFragmentModelMap.put(mModel.id, mModel);
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

    public String getFragmentCode(Class<?> clazz) {
        String code = "";
        Iterator<Entry<String, FragmentModel>> iter = mFragmentModelMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, FragmentModel> entry = (Map.Entry<String, FragmentModel>) iter.next();

            FragmentModel model = entry.getValue();
            if (model.className.equals(clazz.getName())) {
                code = entry.getKey();
                break;
            }

        }

        return code;
    }

}
