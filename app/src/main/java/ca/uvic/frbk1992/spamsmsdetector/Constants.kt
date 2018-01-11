package ca.uvic.frbk1992.spamsmsdetector

/**
 * File with constants
 */

val DEBUG = true

val PHISHING = -1
val LEGITIMATE = 1
val SUSPICIOUS  = 0

val PHISHING_S = "Phishing"
val LEGITIMATE_S = "Legitimate"
val SUSPICIOUS_S  = "Suspicious"

val SMS_LIST_FRAGMENT_TAG = "slft"
val SMS_SPAM_LIST_FRAGMENT_TAG = "sslft"
val SMS_DETAIL_FRAGMENT_TAG = "sdft"
val TEST_URL_FRAGMENT_TAG = "tuft"

val SMS_DETAIL_ACTIVITY = "sms_detail"

val REQUEST_PERMISSION_RECEIVE_SMS = 1


//preferences
var URL_PREFERENCES = "url-preferences"
var HAVING_IP_ADDRRESS  = "pref_1"
var URL_LENGHT   = "pref_2"
var SHORTENING_SERVICE = "pref_3"
var HAVING_AT_SYMBOL   = "pref_4"
var DOUBLE_SLASH_REDIRECTING = "pref_5"
var PREFIX_SUFFIX  = "pref_6"
var HAVING_SUB_DOMAIN  = "pref_7"
var SSL_FINAL_STATE  = "pref_8"
var DOMAIN_REGISTRATION_LENGHT = "pref_9"
var FAVICON = "pref_10"
var PORT = "pref_11"
var HTTPS_TOKEN = "pref_12"
var REQUEST_URL  = "pref_13"
var URL_OF_ANCHOR = "pref_14"
var LINKS_IN_TAGS = "pref_15"
var SFH  = "pref_16"
var SUBMITTING_TO_EMAIL = "pref_17"
var ABNORMAL_URL = "pref_18"
var REDIRECT  = "pref_19"
//var ON_MOUSE_OVER  = "pref_20" //block by today's browsers
var RIGHT_CLICK  = "pref_21"
var POP_UP_WINDOW  = "pref_22"
var I_FFRAME = "pref_23"
var AGE_OF_DOMAIN  = "pref_24"
var DNS_RECORD   = "pref_25"
var WEB_TRAFFIC  = "pref_26"
//var PAGE_RANK = "pref_27" //not use anymore
var GOOGLE_INDEX = "pref_28"
var LINKS_POINTING_TO_PAGE = "pref_29"
var STATISTICAL_REPORT = "pref_30"

val PHISHING_MODEL_FILE = "file:///android_asset/phishing_model_opt4.pb"
val SMS_MODEL_FILE = "file:///android_asset/sms_model_opt4.pb"
val OUTPUT = "output"
val INPUT = "input"

//val BAG_OF_WORDS = arrayOf("call", "that", "your", "have", "free", "will", "from", "just", "mobile", "ltgt",
//                        "text", "when", "stop", "with", "claim", "this", "what", "reply", "know", "prize",
//                        "like", "only", "come", "then", "nokia", "good", "send", "time", "urgent", "there",
//                        "cash", "love", "contact", "going", "service", "i\"ll", "please", "want",
//                        "guaranteed", "home", "customer", "about", "week", "need", "tone", "sorry",
//                        "phone", "still", "been", "later", "chat", "awarded", "back", "draw", "dont",
//                        "mins", "don\"t", "latest", "think", "line", "today", "receive", "camera", "tell",
//                        "every", "some", "message", "take", "holiday", "much", "landline", "here", "shows",
//                        "they", "apply", "well", "more", "night", "pobox", "happy", "number", "great",
//                        "code", "hope", "live", "where", "dear", "video", "work")

val BAG_OF_WORDS = arrayOf("call", "that", "your", "have", "free", "will", "from", "just", "mobile",
        "ltgt", "text", "when", "stop", "with", "claim", "this", "what", "reply", "know", "prize",
        "like", "only", "come", "then", "nokia", "good", "send", "time", "urgent", "there", "cash",
        "love", "contact", "going", "service", "i\'ll", "please", "want", "guaranteed", "home",
        "customer", "about", "week", "need", "tone", "sorry", "phone", "still", "been", "later",
        "chat", "awarded", "back", "draw", "dont", "mins", "don\'t", "latest", "think", "line",
        "today", "receive", "camera", "tell", "every", "some", "message", "take", "holiday", "much",
        "landline", "here", "shows", "they", "apply", "well", "more", "night", "pobox", "happy",
        "number", "great", "code", "hope", "live", "where", "dear", "video", "work", "award",
        "give", "chance", "it\'s", "entry", "already", "ringtone", "right", "make", "pmin",
        "should", "orange", "collection", "really", "network", "yeah", "selected", "doing", "after",
        "offer", "said", "tones", "tomorrow", "weekly", "valid", "them", "cost", "morning", "find", "word",
        "very", "collect", "life", "attempt", "meet", "delivery", "anything", "bonus", "would", "gift",
        "sure", "vouchers", "babe", "music", "pick", "club", "miss", "rate", "something", "update",
        "account", "also")



//val BAG_OF_WORDS = arrayOf("call", "that", "your", "have", "free", "will", "from", "just", "mobile",
//        "ltgt", "text", "when", "stop", "with", "claim", "this", "what", "reply", "know", "prize",
//        "like", "only", "come", "then", "nokia", "good", "send", "time", "urgent", "there", "cash",
//        "love", "contact", "going", "service","i\'ll", "please", "want", "guaranteed", "home",
//        "customer", "about", "week", "need", "tone", "sorry", "phone", "still", "been", "later",
//        "chat", "awarded", "back", "draw", "dont", "mins","don\'t","latest", "think", "line",
//        "today", "receive", "camera", "tell", "every", "some", "message", "take", "holiday", "much",
//        "landline", "here", "shows", "they", "apply", "well", "more", "night", "pobox", "happy",
//        "number", "great", "code", "hope", "live", "where", "dear", "video", "work", "award", "give",
//        "chance","it\'s","entry", "already", "ringtone", "right", "make", "pmin", "should", "orange",
//        "collection", "really", "network", "yeah", "selected", "doing", "after", "offer", "said", "tones",
//        "tomorrow", "weekly", "valid", "them", "cost", "morning", "find", "word", "very", "collect",
//        "life", "attempt", "meet", "delivery", "anything", "bonus", "would", "gift", "sure", "vouchers",
//        "babe", "music", "pick", "club", "miss", "rate", "something", "update", "account", "also", "help",
//        "await", "last","i\'ve","play", "private", "feel", "unsubscribe", "again", "mths", "keep", "price",
//        "care", "poly", "thanks", "land", "went", "colour", "cant", "pounds", "before", "tried", "thing",
//        "waiting", "around", "expires", "todays", "gonna", "services", "sent", "name", "tonight",
//        "double", "wait", "texts", "were","can\'t","final", "nice", "winner", "soon", "days", "first",
//        "next", "always", "statement", "sleep", "unredeemed", "many", "points", "even", "identifier",
//        "down", "join", "over","that\'s","games", "late", "pmsg", "place", "mobileupd", "could", "dating",
//        "money", "told", "voucher", "leave", "which", "auction", "things", "camcorder", "other", "yours",
//        "trying", "friends", "operator", "sexy", "coming", "wish", "xmas","you\'re","tscs", "same",
//        "wkly", "haha", "people", "order", "getting", "content", "hello", "someone", "thought",
//        "congratulations", "fine", "lunch", "done", "quiz", "real","didn\'t","optout", "smile",
//        "year", "easy", "class", "meeting", "txts", "half", "stuff", "worth", "finish", "savamob",
//        "never", "offers", "long", "mates", "having", "freemsg", "better", "mobiles", "talk", "info",
//        "cool", "national", "complimentary", "person", "ampm", "thats", "caller", "mind", "player",
//        "heart", "anytime", "best", "phones", "being", "rental", "calls", "than", "ipod", "important",
//        "because", "friend", "pics", "dinner", "another", "valued", "birthday", "house", "charged",
//        "liao", "special", "ready", "dogging", "problem", "watching", "motorola", "messages", "room",
//        "check", "nothing", "comp", "might", "reward", "shit", "news", "quite", "early", "congrats",
//        "sweet", "representative", "once", "between", "aight", "speak", "watch", "probably", "direct",
//        "world", "txting", "wont", "secret", "admirer", "called", "thinks", "thanx", "unlimited",
//        "plan", "choose", "ever", "charge", "actually", "custcare", "princess", "currently", "remember",
//        "forgot", "welcome", "guess", "either", "dunno", "kiss", "enjoy","he\'s")
