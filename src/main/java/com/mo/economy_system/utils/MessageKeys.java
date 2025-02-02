package com.mo.economy_system.utils;

public class MessageKeys {
    public static final String COIN_COMMAND_BALANCE = "message.coin_command_balance";
    public static final String COIN_COMMAND_ADD = "message.coin_command_add";
    public static final String COIN_COMMAND_MIN = "message.coin_command_min";
    public static final String COIN_COMMAND_INSUFFICIENT_BALANCE = "message.coin_command_insufficient_balance";
    public static final String COIN_COMMAND_SET = "message.coin_command_set";
    public static final String TRANSFER_SUCCESSFULLY_MESSAGE_KEY = "message.transfer.transfer_successfully";
    public static final String RECEIVE_SUCCESSFULLY_MESSAGE_KEY = "message.transfer.receive_successfully";
    public static final String TRANSFER_FAILED_MESSAGE_KEY = "message.transfer.transfer_failed";

    public static final String TPA_SELF_ERROR = "message.tpa.self_error";
    public static final String TPA_NO_POTION = "message.tpa.no_potion";
    public static final String TPA_REQUEST_SENT = "message.tpa.request_sent";
    public static final String TPA_ACCEPT = "message.tpa.accept";
    public static final String TPA_DENY = "message.tpa.deny";
    public static final String TPA_NO_REQUEST = "message.tpa.no_request";
    public static final String TPA_SENDER_OFFLINE = "message.tpa.sender_offline";
    public static final String TPA_SENDER_NO_POTION = "message.tpa.sender_no_potion";
    public static final String TPA_TELEPORTED = "message.tpa.teleported";
    public static final String TPA_ACCEPTED = "message.tpa.accepted";
    public static final String TPA_DENIED = "message.tpa.denied";
    public static final String TPA_TIMEOUT_SENDER = "message.tpa.timeout_sender";
    public static final String TPA_TIMEOUT_TARGET = "message.tpa.timeout_target";

    public static final String RECALL_POTION_ERROR_DIMENSION_NOT_FOUND = "message.recall_potion.error_dimension_not_found";

    public static final String HOME_TITLE_KEY = "screen.home.title";
    public static final String HOME_FETCHING_BALANCE_TEXT_KEY = "text.home.fetching_balance";
    public static final String HOME_BALANCE_TEXT_KEY = "text.home.balance";
    public static final String HOME_SHOP_BUTTON_KEY = "button.home.shop";
    public static final String HOME_MARKET_BUTTON_KEY = "button.home.market";
    public static final String HOME_TERRITORY_BUTTON_KEY = "button.home.territory";
    public static final String HOME_ABOUT_BUTTON_KEY = "button.home.about";

    public static final String SHOP_TITLE_KEY = "screen.shop.title";
    public static final String SHOP_ITEM_PRICE_KEY = "screen.shop.item.price";
    public static final String SHOP_ITEM_ID_KEY = "screen.shop.item.id";
    public static final String SHOP_ITEM_BASIC_PRICE_KEY = "screen.shop.item.basic_price";
    public static final String SHOP_ITEM_CURRENT_PRICE_KEY = "screen.shop.item.current_price";
    public static final String SHOP_ITEM_CHANGE_PRICE_KEY = "screen.shop.item.change_price";
    public static final String SHOP_ITEM_FLUCTUATION_FACTOR_KEY = "screen.shop.item.fluctuation_factor";
    public static final String SHOP_LOADING_SHOP_DATA_TEXT_KEY = "text.shop.loading_shop_data";
    public static final String SHOP_BUY_BUTTON_KEY = "button.shop.buy";
    public static final String SHOP_HINT_TEXT_KEY = "text.shop.hint";
    public static final String SHOP_BUY_SUCCESSFULLY_MESSAGE_KEY = "message.shop.buy_successfully";
    public static final String SHOP_BUY_FAILED_MESSAGE_KEY = "message.shop.buy_failed";
    public static final String SHOP_INVALID_ITEM_MESSAGE_KEY = "message.shop.invalid_item";  // 无效的商品
    public static final String SHOP_BUY_FAILED_INVENTORY_FULL_MESSAGE_KEY = "message.shop.buy_failed_inventory_full";  // 购买失败，库存已满
    public static final String SHOP_BUY_ERROR_MESSAGE_KEY = "message.shop.buy_error";  // 购买错误

    public static final String LIST_TITLE_KEY = "screen.list.title";
    public static final String LIST_NO_ITEM_IN_HAND_TEXT_KEY = "text.list.no_item_in_hand";
    public static final String LIST_PRICE_TEXT_KEY = "text.list.price";
    public static final String LIST_LIST_BUTTON_KEY = "button.list.list";
    public static final String LIST_NO_ITEM_IN_HAND_MESSAGE_KEY = "message.list.no_item_in_hand";
    public static final String LIST_INVALID_PRICE_MESSAGE_KEY = "message.list.invalid_price";
    public static final String LIST_HINT_TEXT_KEY = "text.list.hint";
    public static final String LIST_SUCCESSFULLY_MESSAGE_KEY = "message.list.list_successfully";
    public static final String LIST_INSUFFICIENT_ITEM_MESSAGE_KEY = "message.list.list_insufficient_item";
    public static final String LIST_UNMATCHED_ITEM_MESSAGE_KEY = "message.list.list_unmatched_item";

    public static final String REQUEST_TITLE_KEY = "screen.request.title";
    public static final String REQUEST_PRICE_TEXT_KEY = "text.request.price";
    public static final String REQUEST_ITEM_COUNT_TEXT_KEY = "text.request.item_count";
    public static final String REQUEST_ITEM_ID_TEXT_KEY = "text.request.item_id";
    public static final String REQUEST_REQUEST_BUTTON_KEY = "button.request.request";
    public static final String REQUEST_PRICE_HINT_TEXT_KEY = "text.request.price.hint";
    public static final String REQUEST_ITEM_COUNT_HINT_TEXT_KEY = "text.request.item_count.hint";
    public static final String REQUEST_ITEM_ID_HINT_TEXT_KEY = "text.request.item_id.hint";
    public static final String REQUEST_UNKNOWN_ITEM_ID_KEY = "text.request.unknown_item_id";
    public static final String REQUEST_INVALID_ITEM_COUNT_KEY = "text.request.invalid_item_count";
    public static final String REQUEST_EXCESSIVE_ITEM_COUNT_KEY = "text.request.excessive_item_count";
    public static final String REQUEST_INVALID_PRICE_KEY = "text.request.invalid_price";
    public static final String REQUEST_DELIVER_BUTTON_KEY = "button.request.deliver";
    public static final String REQUEST_DELIVERED_STATUS_KEY = "button.request.delivered_status"; // 已交付
    public static final String REQUEST_CANCEL_KEY = "button.request.cancel"; // 已交付
    public static final String REQUEST_CLAIM_BUTTON_KEY = "button.request.claim"; // 领取
    public static final String DELIVERY_NOT_ENOUGH_ITEMS_KEY = "message.delivery.not_enough_items"; // 你没有足够的物品来交付
    public static final String DELIVERY_SUCCESS_KEY = "message.delivery.success"; // 你成功交付了这个物品
    public static final String CLAIM_SUCCESS_KEY = "message.request.claim.success"; // 成功领取物品
    public static final String CLAIM_NOT_OWNER_KEY = "message.claim.not_owner"; // 你不能领取不属于你的物品
    public static final String ORDER_DELIVERED_BY_PLAYER_KEY = "message.order.delivered_by_player";

    public static final String MARKET_TITLE_KEY = "screen.market.title";
    public static final String MARKET_SELLER_NAME_KEY = "screen.market.item.seller_name";
    public static final String MARKET_SELLER_UUID_KEY = "screen.market.item.seller_uuid";
    public static final String MARKET_TRADE_ID_KEY = "screen.market.item.trade_id";
    public static final String MARKET_ITEM_ID_KEY = "screen.market.item.id";
    public static final String MARKET_ITEM_NAME_AND_COUNT_KEY = "screen.market.item.name_and_count";
    public static final String MARKET_ITEM_PRICE_KEY = "screen.market.item.price";
    public static final String MARKET_NO_ITEMS_TEXT_KEY = "text.market.no_items";
    public static final String MARKET_LIST_BUTTON_KEY = "button.market.list";
    public static final String MARKET_REQUEST_BUTTON_KEY = "button.market.request";
    public static final String MARKET_SWITCH_DISPLAY_TYPE_0_BUTTON_KEY = "button.market.switch_display_type_0";
    public static final String MARKET_SWITCH_DISPLAY_TYPE_1_BUTTON_KEY = "button.market.switch_display_type_1";
    public static final String MARKET_SWITCH_DISPLAY_TYPE_2_BUTTON_KEY = "button.market.switch_display_type_2";
    public static final String MARKET_SWITCH_DISPLAY_TYPE_3_BUTTON_KEY = "button.market.switch_display_type_3";
    public static final String MARKET_SWITCH_DISPLAY_TYPE_4_BUTTON_KEY = "button.market.switch_display_type_4";
    public static final String MARKET_BUY_BUTTON_KEY = "button.market.buy";
    public static final String MARKET_REMOVE_BUTTON_KEY = "button.market.remove";
    public static final String MARKET_HINT_TEXT_KEY = "text.market.hint";
    public static final String MARKET_ITEM_DOES_NOT_EXIST_MESSAGE_KEY = "message.market.item_does_not_exist";
    public static final String MARKET_PURCHASE_FAILED_MESSAGE_KEY = "message.market.purchase_failed";
    public static final String MARKET_PURCHASE_SUCCESSFULLY_MESSAGE_KEY = "message.market.purchase_successfully";
    public static final String MARKET_COLLECT_MONEY_MESSAGE_KEY = "message.market.collect_money";
    public static final String MARKET_REMOVE_FAILED_MESSAGE_KEY = "message.market.remove_failed";
    public static final String MARKET_UNMATCHED_SELLER_MESSAGE_KEY = "message.market.unmatched_seller";
    public static final String MARKET_ITEM_HAS_BEEN_RETURNED_MESSAGE_KEY = "message.market.item_has_been_returned";

    public static final String RED_PACKET_INSUFFICIENT_BALANCE = "message.red_packet.insufficient_balance";
    public static final String RED_PACKET_ALREADY_ACTIVE = "message.red_packet.already_active";
    public static final String RED_PACKET_CREATED_SUCCESSFULLY = "message.red_packet.created_successfully";
    public static final String RED_PACKET_NO_AVAILABLE = "message.red_packet.no_available";
    public static final String RED_PACKET_ALREADY_CLAIMED = "message.red_packet.already_claimed";
    public static final String RED_PACKET_CLAIM_SUCCESS = "message.red_packet.claim_success";
    public static final String RED_PACKET_CLAIM_BUTTON = "message.red_packet.claim_button";
    public static final String RED_PACKET_BROADCAST = "message.red_packet.broadcast";
    public static final String RED_PACKET_NO_ACTIVE = "message.red_packet.no_active";
    public static final String RED_PACKET_CANCELLED = "message.red_packet.cancelled";
    public static final String RED_PACKET_FULLY_CLAIMED = "message.red_packet.fully_claimed";
    public static final String RED_PACKET_EXPIRED_REFUNDED = "message.red_packet.expired_refunded";
    public static final String RED_PACKET_EXPIRED_BROADCAST = "message.red_packet.expired_broadcast";
    public static final String RED_PACKET_CLAIM_BROADCAST = "message.red_packet.claim_broadcast";

    public static final String TERRITORY_TITLE_KEY = "screen.territory.title";
    public static final String TERRITORY_NO_TERRITORIES_TEXT_KEY = "text.territory.no_territories";
    public static final String TERRITORY_TERRITORY_NAME_TEXT_KEY = "text.territory.territory_name";
    public static final String TERRITORY_TERRITORY_AREA_TEXT_KEY = "text.territory.territory_area";
    public static final String TERRITORY_TELEPORT_BUTTON_KEY = "button.territory.teleport";
    public static final String TERRITORY_MANAGE_BUTTON_KEY = "button.territory.manage";
    public static final String TERRITORY_TERRITORY_NAME_KEY = "screen.territory.territory_name";
    public static final String TERRITORY_TERRITORY_UUID_KEY = "screen.territory.territory_uuid";
    public static final String TERRITORY_TERRITORY_OWNER_NAME_KEY = "screen.territory.territory_owner_name";
    public static final String TERRITORY_TERRITORY_OWNER_UUID_KEY = "screen.territory.territory_owner_name_uuid";
    public static final String TERRITORY_TERRITORY_BACK_POINT_KEY = "screen.territory.territory_back_point";
    public static final String TERRITORY_TERRITORY_NO_AUTHORIZED_PLAYER_KEY = "screen.territory.territory_no_authorized_player";

    public static final String TERRITORY_MANAGEMENT_COPY_ID = "message.territory_management.copy_id";
    public static final String TERRITORY_MANAGEMENT_INVITE_PLAYER = "message.territory_management.invite_player";
    public static final String TERRITORY_MANAGEMENT_DELETE_TERRITORY = "message.territory_management.delete_territory";
    public static final String TERRITORY_MANAGEMENT_COPY_SUCCESS = "message.territory_management.copy_success";
    public static final String TERRITORY_MANAGEMENT_KICK_PLAYER = "message.territory_management.kick_player";

    public static final String TERRITORY_NOT_FOUND = "message.territory.not_found";
    public static final String TERRITORY_NO_OWNER_PERMISSION = "message.territory.no_owner_permission";
    public static final String TERRITORY_REMOVE_SUCCESS = "message.territory.remove_success";

    public static final String TERRITORY_NOT_EXIST = "message.territory.not_exist";
    public static final String TERRITORY_NO_PERMISSION = "message.territory.no_permission";
    public static final String TERRITORY_PLAYER_KICKED = "message.territory.player_kicked";
    public static final String TERRITORY_PLAYER_REMOVED = "message.territory.player_removed";

    public static final String INVITE_TITLE_KEY = "screen.invite.title";
    public static final String INVITE_INVITE_BUTTON_KEY = "button.invite.invite";
    public static final String INVITE_NO_NAME_KEY = "message.invite.no_name";
    public static final String INVITE_BACK_BUTTON = "button.invite.back";
    public static final String INVITE_NO_PERMISSION = "message.invite.no_permission"; // 你没有权限邀请玩家加入此领地！
    public static final String INVITE_PLAYER_OFFLINE = "message.invite.player_offline"; // 玩家 {playerName} 不在线！
    public static final String INVITE_ALREADY_MEMBER = "message.invite.already_member"; // 玩家 {playerName} 已经是你的领地成员了！
    public static final String INVITE_SENT = "message.invite.sent"; // 已向玩家 {playerName} 发送邀请！
    public static final String INVITE_RECEIVED = "message.invite.received"; // 玩家 {inviterName} 邀请你加入领地: {territoryName}
    public static final String INVITE_INSTRUCTIONS = "message.invite.instructions"; // 输入 /accept_invite 接受 或 /decline_invite 拒绝
    public static final String INVITE_SELF_ERROR = "message.invite.self_error"; // 你不能向自己发出邀请!

    public static final String INVITE_NOT_IN_TERRITORY = "message.invite.not_in_territory";
    public static final String INVITE_SENT_TO_PLAYER = "message.invite.sent_to_player";
    public static final String INVITE_RECEIVED_PLAYER = "message.invite.received_player";
    public static final String COMMAND_PLAYER_ONLY = "message.command.player_only";
    public static final String TERRITORY_SETBACKPOINT_NO_PERMISSION = "message.territory.setbackpoint.no_permission";
    public static final String TERRITORY_SETBACKPOINT_SUCCESS = "message.territory.setbackpoint.success";
    public static final String INVITE_NO_PENDING = "message.invite.no_pending";
    public static final String INVITE_TARGET_NOT_FOUND = "message.invite.target_not_found";
    public static final String INVITE_ACCEPTED = "message.invite.accepted";
    public static final String INVITE_ACCEPT_BUTTON = "button.invite.accept";
    public static final String INVITE_DECLINE_NO_PENDING = "message.invite.decline_no_pending";
    public static final String INVITE_DECLINED = "message.invite.declined";
    public static final String INVITE_DECLINE_BUTTON = "button.invite.decline";


    public static final String CLAIM_WAND_SELECT_POINTS = "message.claim_wand.select_points";
    public static final String CLAIM_INSUFFICIENT_BALANCE = "message.claim.insufficient_balance";
    public static final String CLAIM_SUCCESS = "message.claim.success";
    public static final String CLAIM_WAND_FIRST_POSITION_SET = "message.claim_wand.first_position_set";
    public static final String CLAIM_WAND_SECOND_POSITION_SET = "message.claim_wand.second_position_set";
    public static final String CLAIM_WAND_OVERLAP_ERROR = "message.claim_wand.overlap_error";
    public static final String CLAIM_WAND_VOLUME = "message.claim_wand.volume";
    public static final String CLAIM_WAND_PRICE = "message.claim_wand.price";
    public static final String CLAIM_WAND_INSTRUCTION = "message.claim_wand.instruction";
    public static final String CLAIM_WAND_CANCEL = "message.claim_wand.cancel";
    public static final String CLAIM_WAND_TIMEOUT = "message.claim_wand.timeout";

    public static final String TELEPORT_TARGET_NOT_FOUND = "message.teleport.target_not_found";
    public static final String TELEPORT_NO_PERMISSION = "message.teleport.no_permission";
    public static final String TELEPORT_NO_BACKPOINT = "message.teleport.no_backpoint";
    public static final String TELEPORT_DIMENSION_NOT_FOUND = "message.teleport.dimension_not_found";
    public static final String TELEPORT_NO_POTION = "message.teleport.no_potion";
    public static final String TELEPORT_SUCCESS = "message.teleport.success";
    public static final String TELEPORT_FAILED = "message.teleport.failed";

    public static final String ABOUT_TITLE_KEY = "screen.about.title";
    public static final String ABOUT_MOD_NAME_KEY = "screen.about.mod_name";
    public static final String ABOUT_AUTHOR_NAME_KEY = "screen.about.author_name";
    public static final String ABOUT_GITHUB_URL_KEY = "screen.about.github_url";
    public static final String ABOUT_TEXT_SHOW_KEY = "screen.about.show_github_text";
    public static final String ABOUT_COPY_URL = "message.about.copy_github_url";
    public static final String ABOUT_BACK_BUTTON_KEY = "button.about.back";
}

