package com.google.glass.companion;

import java.util.UUID;

public class CompanionConstants
{
  public static final String ACTION_INPUT_BOX_RESPONSE = "com.google.glass.companion.INPUT_BOX_RESPONSE";
  public static final String ACTION_SETUP_GLASS = "com.google.glass.companion.SETUP_GLASS";
  public static final String ACTION_SETUP_WIFI = "com.google.glass.companion.SETUP_WIFI";
  public static final String EXTRA_INPUT_BOX_REQUEST_BYTES = "input_box_request_bytes";
  public static final String EXTRA_INPUT_BOX_RESPONSE_BYTES = "input_box_response_bytes";
  public static final String EXTRA_SETUP_STRING = "setup_string";
  public static final String EXTRA_SETUP_WIFI = "wifi_setup_string";
  public static final UUID SECURE_UUID = UUID.fromString("F15CC914-E4BC-45CE-9930-CB7695385850");
  public static final String SOCKET_NAME = "Companion";
  public static final int VERSION = 131078;
}