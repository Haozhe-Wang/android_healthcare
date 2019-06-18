package com.example.healthcare.dummy;

import com.example.healthcare.colorRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PatientListTitle {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

//    private static final int COUNT = 25;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    public static DummyItem addItem(final String patientUID, final colorRange conditionColor,
                                final String patientName) {
        DummyItem item = new DummyItem(patientUID,conditionColor,patientName);
        ITEMS.add(item);
        ITEM_MAP.put(item.patientUID, item);
        return item;
    }

//    private static DummyItem createDummyItem(final String patientUID, final colorRange conditionColor,
//                                             final String patientName ) {
//        return new DummyItem(patientUID,conditionColor,patientName);
//    }

//    private static String makeDetails(int position) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Details about Item: ").append(position);
//        for (int i = 0; i < position; i++) {
//            builder.append("\nMore details information here.");
//        }
//        return builder.toString();
//    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        private final String patientUID;
        private String patientName;
        private colorRange conditionColor;
        private int position;

        public DummyItem(final String patientUID, final colorRange conditionColor
                            , final String patientName) {
            this.patientUID = patientUID;
            this.conditionColor = conditionColor;
            this.patientName= patientName;
        }
        public String getPatientUID(){
            return patientUID;
        }
        public colorRange getConditionColor(){
            if (this.conditionColor == null){
                return colorRange.NO_COLOR;
            }
            return this.conditionColor;
        }
        public String getPatientName(){
            if (this.patientName == null){
                return "DATA ERROR";
            }
            return this.patientName;
        }
        public int getPosition(){
            return position;
        }
        public void setConditionColor(colorRange conditionColor){this.conditionColor=conditionColor;}
        public void setPatientName(String patientName){this.patientName=patientName;}
        public void setPosition(final int position){this.position=position;}


        @Override
        public String toString() {
            return patientName+" : "+conditionColor;
        }
    }
}
