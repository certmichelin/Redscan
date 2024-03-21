<?php
$g_db_type               = 'mysqli';
$g_hostname              = getenv('MYSQL_HOST') !== false ? getenv('MYSQL_HOST') : 'db';
$g_database_name         = getenv('MYSQL_DATABASE') !== false ? getenv('MYSQL_DATABASE') : 'bugtracker';
$g_db_username           = getenv('MYSQL_USER') !== false ? getenv('MYSQL_USER') : 'mantis';
$g_db_password           = getenv('MYSQL_PASSWORD') !== false ? getenv('MYSQL_PASSWORD') : 'mantis';
$g_crypto_master_salt    = getenv('MASTER_SALT');
$g_webmaster_email       = getenv('EMAIL_WEBMASTER') !== false ? getenv('EMAIL_WEBMASTER') : null;
$g_from_email            = getenv('EMAIL_FROM') !== false ? getenv('EMAIL_FROM') : null;
$g_return_path_email     = getenv('EMAIL_RETURN_PATH') !== false ? getenv('EMAIL_RETURN_PATH') : null;
$g_path                  = getenv('BASE_URL') !== false ? getenv('BASE_URL') : "https://localhost/mantisbt/";
$g_short_path            = getenv('SHORT_PATH') !== false ? getenv('SHORT_PATH') : "/mantisbt/";

//Force english as Mantis BT language
$g_language_choices_arr = array( 'english');

//Customize priority.
$g_priority_enum_string = '1:P1,2:P2,3:P3,4:P4,5:P5';
$g_default_bug_priority = 5;

$g_status_icon_arr = array (
    P4 => 'fa-chevron-down fa-lg green',
	P3 => 'fa-minus fa-lg orange2',
    P2 => 'fa-arrow-up fa-lg red',
	P1 => 'fa-exclamation-triangle fa-lg red'
);

//Customize severity.
$g_severity_enum_string  = '1:Critical,2:High,3:Medium,4:Low,5:Info';

//Customize status.
$g_status_enum_string = '10:new,40:confirmed,60:ongoing,70:toverify,90:closed';

$g_status_colors['new']       = '#D77A6A';
$g_status_colors['confirmed'] = '#D15B47';
$g_status_colors['ongoing']   = '#FEE188';
$g_status_colors['toverify']  = '#8EFE88';
$g_status_colors['closed']    = '#0E8D00';

//Customize workflow.
$g_status_enum_workflow[NEW_]         ='40:confirmed,90:closed';
$g_status_enum_workflow[CONFIRMED]    ='60:ongoing';
$g_status_enum_workflow[ONGOING]      ='70:toverify,90:closed';
$g_status_enum_workflow[TOVERIFY]     ='40:confirmed,90:closed';
$g_status_enum_workflow[CLOSED]       ='10:new';

//Customize resolution.
$g_resolution_enum_string = '10:open,30:reopened,40:unable to duplicate,60:duplicate,90:wont fix,20:fixed';

//Prune useless fields.
$g_enable_profiles = OFF;
$g_bug_view_page_fields = array(
	'attachments',
	'date_submitted',
	'description',
	'handler',
	'id',
	'last_updated',
	'priority',
	'resolution',
	'status',
	'summary',
	'tags',
);

$g_bug_update_page_fields = array(
	'attachments',
	'date_submitted',
	'description',
	'id',
	'last_updated',
	'priority',
	'resolution',
	'status',
	'summary',
	'tags',
);

$g_bug_report_page_fields = array(
    'category_id',
	'attachments',
	'date_submitted',
	'description',
	'id',
	'last_updated',
	'priority',
	'resolution',
	'status',
	'summary',
	'tags',
);

?>