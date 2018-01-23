package com.personal.frbk1992.spamsmsdetector

/**
 * File with constants
 */

const val DEBUG = true

const val PHISHING = -1
const val LEGITIMATE = 1
const val SUSPICIOUS  = 0

const val PHISHING_S = "Phishing"
const val LEGITIMATE_S = "Legitimate"
const val SUSPICIOUS_S  = "Suspicious"

const val SMS_LIST_FRAGMENT_TAG = "slft"
const val SMS_SPAM_LIST_FRAGMENT_TAG = "sslft"
const val SMS_DETAIL_FRAGMENT_TAG = "sdft"
const val APP_INFO_FRAGMENT_TAG = "aift"
const val TEST_URL_FRAGMENT_TAG = "tuft"

const val SMS_DETAIL_ACTIVITY = "sms_detail"

const val REQUEST_PERMISSION_RECEIVE_SMS = 1

const val EXIT = "EXIT"


//preferences
const val URL_PREFERENCES = "url-preferences"
const val HAVING_IP_ADDRRESS  = "pref_1"
const val URL_LENGHT   = "pref_2"
const val SHORTENING_SERVICE = "pref_3"
const val HAVING_AT_SYMBOL   = "pref_4"
const val DOUBLE_SLASH_REDIRECTING = "pref_5"
const val PREFIX_SUFFIX  = "pref_6"
const val HAVING_SUB_DOMAIN  = "pref_7"
const val SSL_FINAL_STATE  = "pref_8"
const val DOMAIN_REGISTRATION_LENGHT = "pref_9"
const val FAVICON = "pref_10"
const val PORT = "pref_11"
const val HTTPS_TOKEN = "pref_12"
const val REQUEST_URL  = "pref_13"
const val URL_OF_ANCHOR = "pref_14"
const val LINKS_IN_TAGS = "pref_15"
const val SFH  = "pref_16"
const val SUBMITTING_TO_EMAIL = "pref_17"
const val ABNORMAL_URL = "pref_18"
const val REDIRECT  = "pref_19"
//const val ON_MOUSE_OVER  = "pref_20" //block by today's browsers
const val RIGHT_CLICK  = "pref_21"
const val POP_UP_WINDOW  = "pref_22"
const val I_FFRAME = "pref_23"
const val AGE_OF_DOMAIN  = "pref_24"
const val DNS_RECORD   = "pref_25"
const val WEB_TRAFFIC  = "pref_26"
//const val PAGE_RANK = "pref_27" //not use anymore
const val GOOGLE_INDEX = "pref_28"
const val LINKS_POINTING_TO_PAGE = "pref_29"
const val STATISTICAL_REPORT = "pref_30"

const val PHISHING_MODEL_FILE = "file:///android_asset/phishing_model_opt4.pb"
const val SMS_MODEL_FILE = "file:///android_asset/sms_model_opt4.pb"
const val OUTPUT = "output"
const val INPUT = "input"

const val APACHE_LICENSE = "apache_license.txt"

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
