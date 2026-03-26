package naeil.gen_coupon.dto.external.playauto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayAutoOrderHistoryResponseDTO {

    @JsonProperty("uniq")
    private String uniq;

    @JsonProperty("shop_cd")
    private String shopCode;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("shop_ord_no_real")
    private String shopOrdNoReal;

    @JsonProperty("shop_sale_no")
    private String shopSaleNo;

    @JsonProperty("shop_sale_name")
    private String shopSaleName;

    @JsonProperty("ord_time")
    private String ordTime;

    @JsonProperty("pay_time")
    private String payTime;

    @JsonProperty("ord_status_mdate")
    private String confirmDate;

    @JsonProperty("api_read_status")
    private String apiReadStatus;
    // NONE: api 미수집 주문
    // CALLED: api 수집 + 주문정보 미수정
    // EDITED: api 수집 + 주문정보 수정

    @JsonProperty("pay_amt")
    private Integer payAmt;

    @JsonProperty("discount_amt")
    private Integer discountAmt;

    @JsonProperty("shop_discount")
    private Integer shopDiscount;

    @JsonProperty("seller_discount")
    private Integer sellerDiscount;

    @JsonProperty("coupon_discount")
    private Integer couponDiscount;

    @JsonProperty("point_discount")
    private Integer pointDiscount;

    @JsonProperty("sales")
    private Integer sales;

    @JsonProperty("order_name")
    private String orderName; // 주문자명

    @JsonProperty("order_id")
    private String orderId; // 주문자 아이디

    @JsonProperty("order_tel")
    private String orderTel; // 주문자 전화번호

    @JsonProperty("order_htel")
    private String orderHtel; // 주문자 휴대폰

    @JsonProperty("order_email")
    private String orderEmail;

    // --- Product Level Fields (from results_prod) ---
    private Integer prodNo;
    private String prodName;
    private Integer optSaleCnt;
    private Integer ordOptSeq;
    private String internalUniq; // Generated unique key for DB

}
