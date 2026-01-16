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
        INSERT INTO BILLING (
            billing_id,
            member_id,
            billing_month,
            total_amount,
            created_at
        )
        VALUES (?, ?, ?, ?, ?)
        
        """;
    public static final String INSERT_BILLING_ITEM =
        """
        INSERT INTO BILLING_ITEM (
            billing_id,
            category,
            item_name,
            amount
        )
        VALUES (?, ?, ?, ?)
        
        """;

    public static final String SELECT_JOINED_DATA = """
        SELECT m.member_id, i.category, i.name as item_name, u.amount
        FROM MEMBER m
        JOIN USAGE_HISTORY u ON m.member_id = u.member_id
        JOIN ITEM i ON u.item_id = i.item_id 
        WHERE u.usage_date BETWEEN ? AND ?
        ORDER BY m.member_id
    """;

    public static final String INSERT_MESSAGE = """
    INSERT INTO MESSAGE (
        message_id, 
        member_id, 
        billing_id, 
        channel, 
        status, 
        reserved_at, 
        created_at, 
        template_code
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
""";
}