package com.elsdoerfer.android.autostarts;

import java.util.LinkedHashMap;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

/**
 * The broadcast actions/intents we know about. This allows us to show
 * some basic information and a prettyfied title to the user.
 *
 * TODO: This is becoming hard to  manage. Convert this to an XML file,
 * with additional information like version added etc. When running an
 * application using new events on an old version of Android, we could
 * hide those events, or mark them as not active.
 */
final class Actions {

	static final Object[][] ALL = {
		// Those our users care most about, we'd like to have those in front.
		{ "android.intent.action.PRE_BOOT_COMPLETED", R.string.act_pre_boot_completed, R.string.act_pre_boot_completed_detail, 0},
		{ Intent.ACTION_BOOT_COMPLETED, R.string.act_boot_completed, R.string.act_boot_completed_detail, 0},
		{ ConnectivityManager.CONNECTIVITY_ACTION, R.string.act_connectivity, R.string.act_connectivity_detail, 0},

		// android.intent.*
		{ Intent.ACTION_AIRPLANE_MODE_CHANGED, R.string.act_airplane_mode_changed, R.string.act_airplane_mode_changed_detail, 0},
		{ Intent.ACTION_BATTERY_CHANGED, R.string.act_battery_changed, R.string.act_battery_changed_detail, 0},
		{ Intent.ACTION_BATTERY_LOW, R.string.act_battery_low, R.string.act_battery_low_detail, 0},
		{ Intent.ACTION_BATTERY_OKAY, R.string.act_battery_okay, R.string.act_battery_okay_detail, 0},   // Added in 1.6
		{ Intent.ACTION_CLOSE_SYSTEM_DIALOGS, R.string.act_close_system_dialogs, R.string.act_close_system_dialogs_detail, 0},
		{ Intent.ACTION_CONFIGURATION_CHANGED, R.string.act_configuration_changed, R.string.act_configuration_changed_detail, 0},
		{ Intent.ACTION_LOCALE_CHANGED, R.string.act_locale_changed, R.string.act_locale_changed_detail, 0},  // new in api level 7.
		{ Intent.ACTION_DATE_CHANGED, R.string.act_date_changed, R.string.act_date_changed_detail, 0},
		{ Intent.ACTION_DEVICE_STORAGE_LOW, R.string.act_device_storage_low, R.string.act_device_storage_low_detail, 0},
		{ Intent.ACTION_DEVICE_STORAGE_OK, R.string.act_device_storage_ok, R.string.act_device_storage_ok_detail, 0},
		{ Intent.ACTION_GTALK_SERVICE_CONNECTED, R.string.act_gtalk_service_connected, R.string.act_gtalk_service_connected_detail, 0},
		{ Intent.ACTION_GTALK_SERVICE_DISCONNECTED, R.string.act_gtalk_service_disconnected, R.string.act_gtalk_service_disconnected_detail, 0},
		{ Intent.ACTION_HEADSET_PLUG, R.string.act_headset_plug, R.string.act_headset_plug_detail, 0},
		{ Intent.ACTION_INPUT_METHOD_CHANGED, R.string.act_input_method_changed, R.string.act_input_method_changed_detail, 0},
		{ Intent.ACTION_MANAGE_PACKAGE_STORAGE, R.string.act_manage_package_storage, R.string.act_manage_package_storage_detail, 0},
		{ Intent.ACTION_CAMERA_BUTTON, R.string.act_camera_button, R.string.act_camera_button_detail, 0},
		{ Intent.ACTION_MEDIA_BUTTON, R.string.act_media_button, R.string.act_media_button_detail, 0},
		{ Intent.ACTION_MEDIA_BAD_REMOVAL, R.string.act_media_bad_removal, R.string.act_media_bad_removal_detail, 0},
		{ Intent.ACTION_MEDIA_CHECKING, R.string.act_media_checking, R.string.act_media_checking_detail, 0},
		{ Intent.ACTION_MEDIA_EJECT, R.string.act_media_eject, R.string.act_media_eject_detail, 0},
		{ Intent.ACTION_MEDIA_MOUNTED, R.string.act_media_mounted, R.string.act_media_mounted_detail, 0},
		{ Intent.ACTION_MEDIA_NOFS, R.string.act_media_nofs, R.string.act_media_nofs_detail, 0},
		{ Intent.ACTION_MEDIA_REMOVED, R.string.act_media_removed, R.string.act_media_removed_detail, 0},
		{ Intent.ACTION_MEDIA_SCANNER_FINISHED, R.string.act_media_scanner_finished, R.string.act_media_scanner_finished_detail, 0},
		{ Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, R.string.act_media_scanner_scan_file, R.string.act_media_scanner_scan_file_detail, 0},
		{ Intent.ACTION_MEDIA_SCANNER_STARTED, R.string.act_media_scanner_started, R.string.act_media_scanner_started_detail, 0},
		{ Intent.ACTION_MEDIA_SHARED, R.string.act_media_shared, R.string.act_media_shared_detail, 0},
		{ Intent.ACTION_MEDIA_UNMOUNTABLE, R.string.act_media_unmountable, R.string.act_media_unmountable_detail, 0},
		{ Intent.ACTION_MEDIA_UNMOUNTED, R.string.act_media_unmounted, R.string.act_media_unmounted_detail, 0},
		{ Intent.ACTION_NEW_OUTGOING_CALL, R.string.act_new_outgoing_call, R.string.act_new_outgoing_call_detail, 0},
		{ Intent.ACTION_PACKAGE_ADDED, R.string.act_package_added, R.string.act_package_added_detail, 0},
		{ Intent.ACTION_PACKAGE_CHANGED, R.string.act_package_changed, R.string.act_package_changed_detail, 0},
		{ Intent.ACTION_PACKAGE_DATA_CLEARED, R.string.act_package_data_cleared, R.string.act_package_data_cleared_detail, 0},
		{ Intent.ACTION_PACKAGE_INSTALL, R.string.act_package_install, R.string.act_package_install_detail, 0},
		{ Intent.ACTION_PACKAGE_REMOVED, R.string.act_package_removed, R.string.act_package_removed_detail, 0},
		{ Intent.ACTION_PACKAGE_REPLACED, R.string.act_package_replaced, R.string.act_package_replaced_detail, 0},
		{ Intent.ACTION_PACKAGE_RESTARTED, R.string.act_package_restarted, R.string.act_package_restarted_detail, 0},
		{ Intent.ACTION_PROVIDER_CHANGED, R.string.act_provider_changed, R.string.act_provider_changed_detail, 0},
		{ Intent.ACTION_REBOOT, R.string.act_reboot, R.string.act_reboot_detail, 0},
		{ Intent.ACTION_SCREEN_OFF, R.string.act_screen_off, R.string.act_screen_off_detail, 0},
		{ Intent.ACTION_SCREEN_ON, R.string.act_screen_on, R.string.act_screen_on_detail, 0},
		{ Intent.ACTION_TIMEZONE_CHANGED, R.string.act_timezone_changed, R.string.act_timezone_changed_detail, 0},
		{ Intent.ACTION_TIME_CHANGED, R.string.act_time_changed, R.string.act_time_changed_detail, 0},
		{ Intent.ACTION_TIME_TICK, R.string.act_time_tick, R.string.act_time_tick_detail, 0},           // not through manifest components?
		{ Intent.ACTION_UID_REMOVED, R.string.act_uid_removed, R.string.act_uid_removed_detail, 0},
		{ Intent.ACTION_UMS_CONNECTED, R.string.act_ums_connected, R.string.act_ums_connected_detail, 0},
		{ Intent.ACTION_UMS_DISCONNECTED, R.string.act_ums_disconnected, R.string.act_ums_disconnected_detail, 0},
		{ Intent.ACTION_USER_PRESENT, R.string.act_user_present, R.string.act_user_present_detail, 0},
		{ Intent.ACTION_WALLPAPER_CHANGED, R.string.act_wallpaper_changed, R.string.act_wallpaper_changed_detail, 0},
		{ Intent.ACTION_POWER_CONNECTED, R.string.act_power_connected, R.string.act_power_connected_detail, 0},
		{ Intent.ACTION_POWER_DISCONNECTED, R.string.act_power_disconnected, R.string.act_power_disconnected_detail, 0},
		{ Intent.ACTION_SHUTDOWN, R.string.act_shutdown, R.string.act_shutdown_detail, 0},
		{ Intent.ACTION_DOCK_EVENT, R.string.act_dock_event, R.string.act_dock_event_detail, 0},
		{ "android.intent.action.ANR", R.string.act_anr, R.string.act_anr_detail, 0},
		{ "android.intent.action.EVENT_REMINDER", R.string.act_event_reminder, R.string.act_event_reminder_detail, 0},
		{ "android.accounts.LOGIN_ACCOUNTS_CHANGED", R.string.act_login_accounts_changed, R.string.act_login_accounts_changed_detail, 0},
		{ "android.intent.action.STATISTICS_REPORT", R.string.act_statistics_report, R.string.act_statistics_report_detail, 0},
		{ "android.intent.action.MASTER_CLEAR", R.string.act_master_clear, R.string.act_master_clear_detail, 0},
		{ "com.android.sync.SYNC_CONN_STATUS_CHANGED", R.string.act_sync_connection_setting_changed, R.string.act_sync_connection_setting_changed_detail, 0}, // SYNC_CONNECTION_SETTING_CHANGED_INTENT
		{ "android.bluetooth.headset.action.STATE_CHANGED", R.string.act_bt_state_changed, R.string.act_bt_state_changed_detail, 0},
		// New in API Level 11:
		{ "android.intent.action.PROXY_CHANGE", R.string.act_proxy_changed, R.string.act_proxy_changed_detail, 0},
		// Added in API Level 4:
		{ "android.search.action.SETTINGS_CHANGED", R.string.act_search_settings_changed, R.string.act_search_settings_changed_detail, 0},
		{ "android.search.action.SEARCHABLES_CHANGED", R.string.act_searchables_changed, R.string.act_searchables_changed_detail, 0},
		// Added in API level 9:
		{ "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED", R.string.act_download_notification_clicked, R.string.act_download_notification_clicked_detail, 0},
		{ "android.intent.action.DOWNLOAD_COMPLETED", R.string.act_download_completed, R.string.act_download_completed_detail, 0},
		{ "android.location.PROVIDERS_CHANGED", R.string.act_location_providers_changed, R.string.act_location_providers_changed_detail, 0},
		{ "android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION", R.string.act_open_audio_effect_session, R.string.act_open_audio_effect_session_detail, 0},
		{ "android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION", R.string.act_close_audio_effect_session, R.string.act_close_audio_effect_session_detail, 0},
		// New in API Level 8:
		{ Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE, R.string.act_external_apps_available, R.string.act_external_apps_available_detail, 0},
		{ Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE, R.string.act_external_apps_unavailable, R.string.act_external_apps_unavailable_detail, 0},
		{ "android.app.action.ACTION_PASSWORD_CHANGED" , R.string.act_password_changed, R.string.act_password_changed_detail, 0},
		{ "android.app.action.ACTION_PASSWORD_FAILED", R.string.act_password_failed, R.string.act_password_failed_detail, 0},
		{ "android.app.action.ACTION_PASSWORD_SUCCEEDED", R.string.act_password_succeeded, R.string.act_password_succeeded_detail, 0},
		{ "android.app.action.DEVICE_ADMIN_DISABLED", R.string.act_device_admin_disabled, R.string.act_device_admin_disabled_detail, 0},
		{ "android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED", R.string.act_device_admin_disable_req, R.string.act_device_admin_disable_req_detail, 0},
		{ "android.app.action.DEVICE_ADMIN_ENABLED", R.string.act_device_admin_enabled, R.string.act_device_admin_enabled_detail, 0},
		// New in API Level 11:
		{ "android.app.action.ACTION_PASSWORD_EXPIRING", R.string.act_password_expiring, R.string.act_password_expiring_detail, 0},

		// com.android.launcher.*
		{ "com.android.launcher.action.INSTALL_SHORTCUT", R.string.act_install_shortcut, R.string.act_install_shortcut_detail, 0},
		{ "com.android.launcher.action.UNINSTALL_SHORTCUT", R.string.act_uninstall_shortcut, R.string.act_uninstall_shortcut_detail, 0},

		// com.android.camera.*
		{ "com.android.camera.NEW_PICTURE", R.string.act_new_picture, R.string.act_new_picture_detail, 0},

		// TelephonyManager
		{ ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED, R.string.act_background_data_setting_changed, R.string.act_background_data_setting_changed_detail, 0},
		{ TelephonyManager.ACTION_PHONE_STATE_CHANGED, R.string.act_phone_state_changed, R.string.act_phone_state_changed_detail, 0},

		// telephony/TelephonyIntents.java
		{ "android.intent.action.SERVICE_STATE", R.string.act_service_state, R.string.act_service_state_detail, 0},
		{ "android.intent.action.ANY_DATA_STATE", R.string.act_any_data_state, R.string.act_any_data_state_detail, 0},
		{ "android.intent.action.SIG_STR", R.string.act_signal_strength, R.string.act_signal_strength_detail, 0},
		{ "android.intent.action.DATA_CONNECTION_FAILED", R.string.act_data_connection_failed, R.string.act_data_connection_failed_detail, 0},
		{ "android.intent.action.NETWORK_SET_TIME", R.string.act_network_set_time, R.string.act_network_set_time_detail, 0},
		{ "ndroid.intent.action.NETWORK_SET_TIMEZONE", R.string.act_network_set_timezone, R.string.act_network_set_timezone_detail, 0},
		{ "android.intent.action.SIM_STATE_CHANGED", R.string.act_sim_state_changed, R.string.act_sim_state_changed_detail, 0},

		// android.provider.Telephony.*
		{ "android.provider.Telephony.SIM_FULL", R.string.act_sim_full, R.string.act_sim_full_detail, 0},
		{ "android.provider.Telephony.SMS_RECEIVED", R.string.act_sms_received, R.string.act_sms_received_detail, 0},
		{ "android.intent.action.DATA_SMS_RECEIVED", R.string.act_data_sms_received, R.string.act_data_sms_received_detail, 0},   // diff namespace, but fits here.
		{ "android.provider.Telephony.SMS_REJECTED", R.string.act_sms_rejected, R.string.act_sms_rejected_detail, 0},  // new in 2.0
		{ "android.provider.Telephony.WAP_PUSH_RECEIVED", R.string.act_wap_push_received, R.string.act_wap_push_received_detail, 0},
		{ "android.provider.Telephony.SECRET_CODE", R.string.act_secret_code, R.string.act_secret_code_detail, 0},   // not part of the public SDK
		{ "android.provider.Telephony.SPN_STRINGS_UPDATED", R.string.act_spn_strings_updated, R.string.act_spn_strings_updated_detail, 0},  // not part of the public SDK

		// android.net.wifi.*
		{ WifiManager.WIFI_STATE_CHANGED_ACTION, R.string.act_wifi_state_changed, R.string.act_wifi_state_changed_detail, 0},
		{ WifiManager.NETWORK_IDS_CHANGED_ACTION, R.string.act_network_ids_changed, R.string.act_network_ids_changed_detail, 0},
		{ WifiManager.RSSI_CHANGED_ACTION, R.string.act_rssi_changed, R.string.act_rssi_changed_detail, 0},
		{ WifiManager.SCAN_RESULTS_AVAILABLE_ACTION, R.string.act_scan_results_available, R.string.act_scan_results_available_detail, 0},
		{ WifiManager.NETWORK_STATE_CHANGED_ACTION, R.string.act_network_state_changed, R.string.act_network_state_changed_detail, 0},
		{ WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION, R.string.act_supplicant_connection_change, R.string.act_supplicant_connection_change_detail, 0},
		{ WifiManager.SUPPLICANT_STATE_CHANGED_ACTION, R.string.act_suplicant_state_changed, R.string.act_suplicant_state_changed_detail, 0},

		// android.media.*
		{ AudioManager.RINGER_MODE_CHANGED_ACTION, R.string.act_ringer_mode_changed, R.string.act_ringer_mode_changed_detail, 0},
		{ AudioManager.VIBRATE_SETTING_CHANGED_ACTION, R.string.act_vibrate_setting_changed, R.string.act_vibrate_setting_changed_detail, 0},
		{ AudioManager.ACTION_AUDIO_BECOMING_NOISY, R.string.act_audio_becoming_noisy, R.string.act_audio_becoming_noisy_detail, 0},  // POTENTIALLY NOT IN "broadcast_actions.txt"!

		// android.speech.tts.* (new in 1.6)
		{ "android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED", R.string.act_tts_queue_completed, R.string.act_tts_queue_completed_detail, 0},
		{ "android.speech.tts.engine.TTS_DATA_INSTALLED", R.string.act_tts_data_installed, R.string.act_tts_data_installed_detail, 0},

		// android.bluetooth.* (officially introduced in 2.0)
		{ "android.bluetooth.adapter.action.DISCOVERY_FINISHED", R.string.act_bt_discovery_finished, R.string.act_bt_discovery_finished_detail, 0},
		{ "android.bluetooth.adapter.action.DISCOVERY_STARTED", R.string.act_discovery_started, R.string.act_discovery_started_detail, 0},
		{ "android.bluetooth.adapter.action.LOCAL_NAME_CHANGED", R.string.act_bt_local_name_changed, R.string.act_bt_local_name_changed_detail, 0},
		{ "android.bluetooth.adapter.action.SCAN_MODE_CHANGED", R.string.act_bt_scan_mode_changed, R.string.act_bt_scan_mode_changed_detail, 0}, // see android.bluetooth.intent.action.SCAN_MODE_CHANGED
		{ "android.bluetooth.adapter.action.STATE_CHANGED", R.string.act_bt_state_changed, R.string.act_bt_state_changed_detail, 0},  // see android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED
		{ "android.bluetooth.device.action.PAIRING_REQUEST", R.string.act_pairing_request, R.string.act_pairing_request_detail, 0},   // see android.bluetooth.intent.action.PAIRING_REQUEST
		{ "android.bluetooth.device.action.PAIRING_CANCEL", R.string.act_pairing_cancel, R.string.act_pairing_cancel, 0},   // see android.bluetooth.intent.action.PAIRING_CANCEL
		{ "android.bluetooth.device.action.ACL_CONNECTED", R.string.act_bt_acl_connected, R.string.act_bt_acl_connected_detail, 0},
		{ "android.bluetooth.device.action.ACL_DISCONNECTED", R.string.act_bt_acl_disconnected, R.string.act_bt_acl_disconnected_detail, 0},
		{ "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED", R.string.act_bt_acl_disconnect_requested, R.string.act_bt_acl_disconnect_requested_detail, 0},
		{ "android.bluetooth.device.action.BOND_STATE_CHANGED", R.string.act_bt_bond_state_changed, R.string.act_bt_bond_state_changed_detail, 0}, // see android.bluetooth.intent.action.BOND_STATE_CHANGED_ACTION
		{ "android.bluetooth.device.action.CLASS_CHANGED", R.string.act_bt_class_changed, R.string.act_bt_class_changed_detail, 0},
		{ "android.bluetooth.device.action.FOUND", R.string.act_bt_found, R.string.act_bt_found_detail, 0},
		{ "android.bluetooth.device.action.NAME_CHANGED", R.string.act_bt_name_changed, R.string.act_bt_name_changed_detail, 0},  // see android.bluetooth.intent.action.NAME_CHANGED
		{ "android.bluetooth.devicepicker.action.DEVICE_SELECTED", R.string.act_bt_device_selected, R.string.act_bt_device_selected_detail, 0},
		{ "android.bluetooth.devicepicker.action.LAUNCH", R.string.act_bt_launch, R.string.act_bt_launch_detail, 0},
		// Potentially deprecated in API Level 11 (no longer in broadcast_events.txt).
		{ "android.bluetooth.headset.action.AUDIO_STATE_CHANGED", R.string.act_bt_audio_state_changed, R.string.act_bt_audio_state_changed_detail, 0}, // see android.bluetooth.intent.action.HEADSET_ADUIO_STATE_CHANGED
		{ "android.bluetooth.headset.action.STATE_CHANGED", R.string.act_bt_state_changed, R.string.act_bt_state_changed_detail, 0},
		{ "android.bluetooth.a2dp.action.SINK_STATE_CHANGED", R.string.act_sink_state_changed, R.string.act_sink_state_changed_detail, 0},
		// New in API Level 11
		{ "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGE", R.string.act_bt_a2dp_connection_state_changed, R.string.act_bt_a2dp_connection_state_changed_detail, 0},
		{ "android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED", R.string.act_bt_a2dp_playing_state_changed, R.string.act_bt_a2dp_playing_state_changed_detail, 0},
		{ "android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED", R.string.act_bt_connection_state_changed, R.string.act_bt_connection_state_changed_detail, 0},
		{ "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED", R.string.act_bt_headset_audio_state_changed, R.string.act_bt_headset_audio_state_changed_detail, 0},
		{ "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED", R.string.act_bt_headset_connection_state_changed, R.string.act_bt_headset_connection_state_changed_detail, 0},
		{ "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT", R.string.act_bt_headset_vendor_event, R.string.act_bt_headset_vendor_event_detail, 0},
		// Old deprecated 1.5/1.6 events; they are no longer listed in 2.0's broadcast_events.txt,
		// though I haven't tested whether they are really no longer available as well.
		{ "android.bluetooth.a2dp.intent.action.SINK_STATE_CHANGED", R.string.act_sink_state_changed, R.string.act_sink_state_changed_detail, 0},
		{ "android.bluetooth.intent.action.DISCOVERY_COMPLETED", R.string.act_discovery_completed, R.string.act_discovery_completed_detail, 0},
		{ "android.bluetooth.intent.action.DISCOVERY_STARTED", R.string.act_discovery_started, R.string.act_discovery_started_detail, 0},
		{ "android.bluetooth.intent.action.HEADSET_STATE_CHANGED", R.string.act_headset_state_changed, R.string.act_headset_state_changed_detail, 0},
		{ "android.bluetooth.intent.action.NAME_CHANGED", R.string.act_bt_name_changed, R.string.act_bt_name_changed_detail, 0},  // see android.bluetooth.device.action.NAME_CHANGED
		{ "android.bluetooth.intent.action.PAIRING_REQUEST", R.string.act_pairing_request, R.string.act_pairing_request_detail, 0},  // see android.bluetooth.device.action.PAIRING_REQUEST
		{ "android.bluetooth.intent.action.PAIRING_CANCEL", R.string.act_pairing_cancel, R.string.act_pairing_cancel_detail, 0},  // see android.bluetooth.device.action.PAIRING_CANCEL
		{ "android.bluetooth.intent.action.REMOTE_DEVICE_CONNECTED", R.string.act_remote_device_connected, R.string.act_remote_device_connected_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_DEVICE_DISAPPEARED", R.string.act_remote_device_disappeared, R.string.act_remote_device_disappeared_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_DEVICE_DISCONNECTED", R.string.act_remote_device_disconnected, R.string.act_remote_device_disconnected_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_DEVICE_DISCONNECT_REQUESTED", R.string.act_remote_device_disconnect_requested, R.string.act_remote_device_disconnect_requested_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_DEVICE_FOUND", R.string.act_remote_device_found, R.string.act_remote_device_found_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_NAME_FAILED", R.string.act_remote_name_failed, R.string.act_remote_name_failed_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_NAME_UPDATED", R.string.act_remote_name_updated, R.string.act_remote_name_updated_detail, 0},
		{ "android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED", R.string.act_bt_state_changed, R.string.act_bt_state_changed_detail, 0},  // see android.bluetooth.adapter.action.STATE_CHANGED
		{ "android.bluetooth.intent.action.BOND_STATE_CHANGED_ACTION", R.string.act_bt_bond_state_changed, R.string.act_bt_bond_state_changed_detail, 0},  // see android.bluetooth.device.action.BOND_STATE_CHANGED
		{ "android.bluetooth.intent.action.HEADSET_ADUIO_STATE_CHANGED", R.string.act_bt_audio_state_changed, R.string.act_bt_audio_state_changed_detail, 0},  // see android.bluetooth.headset.action.AUDIO_STATE_CHANGED
		{ "android.bluetooth.intent.action.SCAN_MODE_CHANGED", R.string.act_bt_scan_mode_changed, R.string.act_bt_scan_mode_changed, 0},  // see android.bluetooth.adapter.action.SCAN_MODE_CHANGED
		// Bluetooth stuff we had collected from wherever in the time before 2.0, but which doesn't appear in the
		// broadcast_actions.txt files for any version; since 2.0 apparently redesigned/officially introduced the
		// Bluetooth SDKs, we need  to assume that those are probably gone as well - or some of them might still exist, untested.
		{ "android.bluetooth.intent.action.BONDING_CREATED", R.string.act_bonding_created, R.string.act_bonding_created_detail, 0},
		{ "android.bluetooth.intent.action.BONDING_REMOVED", R.string.act_bonding_removed, R.string.act_bonding_removed_detail, 0},
		{ "android.bluetooth.intent.action.DISABLED", R.string.act_disabled, R.string.act_disabled_detail, 0},
		{ "android.bluetooth.intent.action.ENABLED", R.string.act_enabled, R.string.act_enabled_detail, 0},
		{ "android.bluetooth.intent.action.MODE_CHANGED", R.string.act_mode_changed, R.string.act_mode_changed_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_ALIAS_CHANGED", R.string.act_remote_alias_changed, R.string.act_remote_alias_changed_detail, 0},
		{ "android.bluetooth.intent.action.REMOTE_ALIAS_CLEARED", R.string.act_remote_alias_cleared, R.string.act_remote_alias_cleared_detail, 0},

		// android.appwidget.*
		// Note that except of UPDATE, the others aren't really sent using a
		// broadcast, or at least widgets usually don't handle them using a
		// broadcast receiver. We have them here anyway, just to be safe.
		{ AppWidgetManager.ACTION_APPWIDGET_UPDATE, R.string.act_appwidget_update, R.string.act_appwidget_update_detail, 0},
		{ AppWidgetManager.ACTION_APPWIDGET_ENABLED, R.string.act_appwidget_enabled, R.string.act_appwidget_enabled_detail, 0},
		{ AppWidgetManager.ACTION_APPWIDGET_DISABLED, R.string.act_appwidget_disabled, R.string.act_appwidget_disabled_detail, 0},
		{ AppWidgetManager.ACTION_APPWIDGET_DELETED, R.string.act_appwidget_deleted, R.string.act_appwidget_deleted_detail, 0},

    };

	static LinkedHashMap<String, Object[]> MAP;

	static {
		// Convert the list of available actions (and their data) into
		// a ordered hash map which we are than able to easily query by
		// action name.
		MAP = new LinkedHashMap<String, Object[]>();
		for (int i = 0; i < Actions.ALL.length; ++i) {
			Object[] action = Actions.ALL[i];
			action[3] = i;
			MAP.put((String)action[0], action);			
		}
	}

	/**
	 * Helper to sort actions based on the order in our map.
	 */
	static public int compare(String action1, String action2) {
//		int idx1 = Utils.getHashMapIndex(MAP, action1);
//		int idx2 = Utils.getHashMapIndex(MAP, action2);
//		boolean has1 = (idx1 != -1);
//		boolean has2 = (idx2 != -1);
//		boolean has1 = Actions.MAP.containsKey(action1);
//		boolean has2 = Actions.MAP.containsKey(action2);
		Object[] mapAction1 = MAP.get(action1);
		Object[] mapAction2 = MAP.get(action2);
		boolean has1 = mapAction1 != null;
		boolean has2 = mapAction2 != null;

		// Make sure that unknown intents (-1) are sorted at the bottom.
		if (has1 && has2) {		// contained both 
			return ((Integer)mapAction1[3]).compareTo((Integer) mapAction2[3]);
		} else if (has1) {		//only contained action1
			return -1;
		} else if (has2) {		//only contained action2
			return +1;
		} else {				//contained neither
			return action1.compareToIgnoreCase(action2);
		}
//		if (idx1 == -1 && idx2 == -1)
//			return action1.compareTo(action2);		//contained neither
//		else if (idx1 == -1)	//only contained action2
//			return +1;
//		else if (idx2 == -1)	//only contained action1
//			return -1;
//		else	// contained both 
//			return idx1 - idx2;
	}
	
    private static int secondaryHash(int h) {
        // Doug Lea's supplemental hash function
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}