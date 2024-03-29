package model;

import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class GlobalConstants {

	public static final double MIN_TA = 0;
	public static final double MAX_TA = 95;
	
	public static final Integer MIN_TC = 0;
	public static final Integer MAX_TC = 7;
	
	public static final Integer MIN_BS = 0;
	public static final Integer MAX_BS = 80;
	
	public static final Double UNI_WEIGHT=1.0;
	public static final Double ABV_WEIGHT=12.0;
	public static final Double SPELL_WEIGHT=9.5;

	public static final Integer AUTO_SEGMENT_SIZE=20000;
	public static final double CLEANSING_THRESHOLD = 0.4;
	public static final Integer MANUAL_SEGMENT_SIZE = 100;
	public static final int MAX_BORDER_LUMIN = 240;
	
	public static final boolean MANUAL_FETCH_ALL = true;
	public static final boolean MANUAL_PREORDER = false;
	
	public static final String GOOGLE_TRANSLATE_SCRIPT_1 = "https://script.google.com/macros/s/AKfycbyDwJBx5V5EW17VFZMOlexrzkU-F2Z0ZQhIdMnyBZArAsw3T_M/exec";
	public static final String GOOGLE_TRANSLATE_SCRIPT_2 = "https://script.google.com/macros/s/AKfycbygHFrGJCWPaRbANkZferwxeTtE5qtW28Z2AHHgYtMBEnbT4SQ2/exec";
	public static final String[] GOOGLE_TRANSLATE_SCRIPTS = new String [] {GOOGLE_TRANSLATE_SCRIPT_1,GOOGLE_TRANSLATE_SCRIPT_2};
	
	public static final int MANUAL_CLASSIF_SEPARATOR = 6;
	public static final boolean AUTO_TEXT_FIELD_DUPLICATE_ACTION = true;
    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	public static final boolean PUSH_RULE_BUFFER_BY_BLOCK = false;
	public static final boolean NATIVE_SELECTION_SCROLL = true;
    public static final boolean ENABLE_TRANSLATION = false;
    public static final boolean HIDE_RULE_RESULT_SUGGESTION_WHEN_KNOWN_VALUE = true;
    public static final boolean ALLOW_DESC_SEARCH_BAR_CUSTOMIZATION = true;
    public static final String[] COLLAPSED_COLOMNS_TO_KEEP_VISIBLE = new String[] {"Completion Status"};
	public static final double COLLAPSED_COLOMNS_TO_KEEP_VISIBLE_WIDTH_MULTIPLIER = 0.085;
	public static final double COLLAPSED_COLOMNS_VISIBLE_WIDTH_MULTIPLIER = 0.2;
	public static final String OUT_LOG = "ntcw_out.log.log";
	public static final String ERR_LOG = "ntcw_err.log.log";
	public static final boolean OPEN_LINKS_IN_EXTERNAL = false;
	public static final boolean TURN_OFF_IMAGE_SEARCH_FOR_DESCRIPTION = true;
	public static final boolean DESCRIPTION_RESTORE_PERSISTED_ITEM = true;
    public static final boolean JAVASCRIPT_PDF_RENDER = true;
    public static final boolean FORCE_PDF_IN_VIEWER = true;
	public static final String SEARCH_PANE_LAYOUT_FORCE = "NEW";
    public static final boolean DEDUP_BY_CAR_NAME_INSTEAD_OF_CAR_ID = true;
	public static final double DEDUP_NUM_APPROACHING_THRESH = 0.05;
	public static final boolean DEDUP_CARAC_WISE = false;
	public static final boolean TEMP_APPLY_RULES_ON_LONG_DESC_ONLY = true;
	public static final int DEDUP_INFO_COL_NUMBER = 6;
	public static final boolean DEDUP_SET_TARGET_CARS = false;
    public static final int EXCEL_MAX_ROW_COUNT = 1048575;
    public static final int MAX_DEDUP_PAIRS_IN_MEMORY = 100000;
    public static final boolean ITEM_CHAR_DATA_TO_BE_COPIED_FROM_SAME_CARS_ONLY_WHEN_RECLASSIFIYING = true;
	public static final boolean CHAR_DESC_PATTERN_SPAN_SUPERRULES_ACCROS_CHARS = false;
	public static final boolean PROMOTE_SUGGESTION_TO_APPLIED_OLD_SCHEMA = false;
    public static final boolean IMAGE_SEARCH_THUMBNAIL = true;
	public static final boolean BROWSER_TO_SHARE_POPUP_HEADER = true;
    public static final int MAX_THREADS = 16;


	public static int NUMBER_OF_MANUAL_PROPOSITIONS_OLD = 3;
	public static int NUMBER_OF_MANUAL_PROPOSITIONS = 5;
	public static boolean USE_TAXOIMPORT_NEW_SCHEMA = true;
	
	public static final String MANUAL_PROPS_1 = "1";
	public static final String MANUAL_PROPS_2 = "2";
	public static final String MANUAL_PROPS_3 = "3";
	public static final String MANUAL_PROPS_4 = "4";
	public static final String MANUAL_PROPS_5 = "5";
	public static final int SEARCH_WORD_LARGE = 3;
	public static final boolean DEV_RUNTIME = true;
	public static final boolean REDIRECT_OUTSTREAM = !DEV_RUNTIME;
	public static final boolean REDIRECT_ERR_ONLY = true;
	public static final String TOOL_VERSION = "6 Alpha";
	public static final String HOST_ADDRESS = "localhost";
	//public static final String HOST_ADDRESS = "91.167.182.87";//"82.66.148.154";//"88.190.148.154";
	//public static final String HOST_ADDRESS = "192.168.0.22";

	
	public static final Integer RULE_DISPLAY_FONT_SIZE = 12;
	public static final Integer CHAR_DISPLAY_FONT_SIZE = 12;

	public static final Paint DESC_NEONEC_GREEN = Color.color(30.0/256,161.0/256,133.0/256);
	public static final Paint DESC_NEONEC_GREY = Color.color(191.0/256,191.0/256,191.0/256);

	public static final Paint RULE_DISPLAY_SYNTAX_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_MAIN_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_COMP_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_FOR_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_MG_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_PC_COLOR = Color.WHITE;
	public static final Paint RULE_DISPLAY_DWG_COLOR = Color.WHITE;
	public static final Paint CHAR_UOM_COLOR = Color.WHITE;
	public static final Paint CHAR_TXT_COLOR = Color.WHITE;
	public static final Paint CHAR_NUM_COLOR = Color.WHITE;
	
	public static final String RULE_DISPLAY_SYNTAX_FONT = "Colibri";
	public static final String RULE_DISPLAY_MAIN_FONT = "Colibri";
	public static final String RULE_DISPLAY_COMP_FONT = "Colibri";
	public static final String RULE_DISPLAY_FOR_FONT = "Colibri";
	public static final String RULE_DISPLAY_MG_FONT = "Colibri";
	public static final String RULE_DISPLAY_PC_FONT = "Colibri";
	public static final String RULE_DISPLAY_DWG_FONT = "Colibri";
	public static final String CHAR_UOM_FONT = "Colibri";
	public static final String CHAR_TXT_FONT = "Colibri";
	public static final String CHAR_NUM_FONT = "Colibri";
	
	public static final FontPosture RULE_DISPLAY_SYNTAX_POSTURE = FontPosture.REGULAR;
	public static final FontPosture RULE_DISPLAY_MAIN_POSTURE = FontPosture.REGULAR;
	public static final FontPosture RULE_DISPLAY_COMP_POSTURE = FontPosture.REGULAR;
	public static final FontPosture RULE_DISPLAY_FOR_POSTURE = FontPosture.REGULAR;
	public static final FontPosture RULE_DISPLAY_MG_POSTURE = FontPosture.ITALIC;
	public static final FontPosture RULE_DISPLAY_PC_POSTURE = FontPosture.ITALIC;
	public static final FontPosture RULE_DISPLAY_DWG_POSTURE = FontPosture.REGULAR;
	public static final FontPosture CHAR_UOM_POSTURE = FontPosture.REGULAR;
	public static final FontPosture CHAR_TXT_POSTURE = FontPosture.REGULAR;
	public static final FontPosture CHAR_NUM_POSTURE = FontPosture.REGULAR;
	public static final FontPosture ITALIC_DISPLAY_SYNTAX_POSTURE = FontPosture.ITALIC;
	
	public static final FontWeight RULE_DISPLAY_SYNTAX_WEIGHT = FontWeight.THIN;
	public static final FontWeight RULE_DISPLAY_MAIN_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight RULE_DISPLAY_COMP_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight RULE_DISPLAY_FOR_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight RULE_DISPLAY_MG_WEIGHT = FontWeight.BOLD;
	public static final FontWeight RULE_DISPLAY_PC_WEIGHT = FontWeight.BOLD;
	public static final FontWeight RULE_DISPLAY_DWG_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight CHAR_UOM_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight CHAR_TXT_WEIGHT = FontWeight.EXTRA_BOLD;
	public static final FontWeight CHAR_NUM_WEIGHT = FontWeight.EXTRA_BOLD;
	
	
	public static final boolean MANUAL_CLASSIF_LOAD_LATEST_WITH_UPLOAD_PRIORITY = true;
	public static final int CHAR_DESC_PATTERN_SELECTION_PHRASE_THRES = 0;
	public static final double MAX_UOM_MULTIPLIER_TOLERANCE = 0.05;
	public static final boolean REFRESH_ALL_RULE_ITEMS_ON_UPLOAD = true;
	public static final String DEFAULT_CHARS_CLASS = "DEFAULT_CHARS_CLASS_ID";


	public static long ManualValuesBufferFlushTime = 60L;
	public static int ManualValuesBufferFlushSize = 10;
}
