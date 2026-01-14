package springboot.billgates.domain.billing.sql;

public class BillingSqls {
    public static final String SELECT_MEMBERS = "SELECT member_id, name, email, phone_number FROM MEMBER";
    public static final String SELECT_USAGE_BY_MEMBER =
        """
        SELECT i.category, i.name , u.amount
        FROM USAGE_HISTORY u
        JOIN ITEM i ON u.item_id = i.item_id
        WHERE u.member_id = ?
          AND u.usage_date >= ?
          AND u.usage_date < ?
        """;
    public static final String INSERT_BILLING =
        """
        INSERT INTO BILLING (member_id, billing_month, total_amount, created_at)
        VALUES (?, ?, ?, ?)
        """;
    public static final String INSERT_BILLING_ITEM =
        """
        INSERT INTO BILLING_ITEM (billing_id, category, item_name, amount)
        VALUES (?, ?, ?, ?)
        """;
}
