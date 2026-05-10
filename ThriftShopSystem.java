import java.time.LocalDateTime;
import java.util.*;

// 1. CORE MODELS / ENUMS
enum Role {
    CUSTOMER, ADMIN
}

enum OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

class User {
    long id;
    String username;
    String password;
    Role role;
    String email;
}

class ThriftShop {
    long id;
    String name;
    long ownerId;
}

class Item {
    long id;
    long thriftShopId;
    String name;
    String description;
    double price;
    int stockQuantity;
}

class CartItem {
    long itemId;
    int quantity;
    double price; // snapshot price at time of adding to cart
}

class Cart {
    long customerId;
    List<CartItem> items = new ArrayList<>();
}

class OrderItem {
    long itemId;
    int quantity;
    double price; 
}

class Order {
    long id;
    long customerId;
    long thriftShopId;
    List<OrderItem> items = new ArrayList<>();
    String billingAddress;
    String shippingAddress;
    String paymentMethod;
    double totalAmount;
    double shippingCost;
    double taxAmount;
    double discountAmount;
    OrderStatus status;
    LocalDateTime orderDate;
}

class AuditLog {
    long id;
    long adminId;
    String action;
    LocalDateTime timestamp;
    String details;
}

// 2. DATA STORAGE SKELETON (In-Memory Database Mock)
class DatabaseMock {
    Map<Long, User> users = new HashMap<>();
    Map<Long, ThriftShop> thriftShops = new HashMap<>();
    Map<Long, Item> items = new HashMap<>();
    Map<Long, Cart> carts = new HashMap<>();
    Map<Long, Order> orders = new HashMap<>();
    Map<Long, AuditLog> auditLogs = new HashMap<>();
    
    long nextUserId = 1;
    long nextThriftShopId = 1;
    long nextItemId = 1;
    long nextCartId = 1;
    long nextOrderId = 1;
    long nextAuditLogId = 1;
    
    // Initialize with sample data
    public DatabaseMock() {
        // Sample Admin
        User admin = new User();
        admin.id = nextUserId++;
        admin.username = "admin";
        admin.password = "admin123";
        admin.role = Role.ADMIN;
        admin.email = "admin@thriftshop.com";
        users.put(admin.id, admin);
        
        // Sample Customer
        User customer = new User();
        customer.id = nextUserId++;
        customer.username = "customer1";
        customer.password = "cust123";
        customer.role = Role.CUSTOMER;
        customer.email = "customer1@email.com";
        users.put(customer.id, customer);
        
        // Sample Thrift Shop
        ThriftShop shop = new ThriftShop();
        shop.id = nextThriftShopId++;
        shop.name = "Green Thrift";
        shop.ownerId = admin.id;
        thriftShops.put(shop.id, shop);
        
        // Sample Items
        createSampleItem(shop.id, "Old Jeans", "Vintage blue jeans", 15.99, 10);
        createSampleItem(shop.id, "Leather Jacket", "Genuine leather jacket", 45.50, 5);
        createSampleItem(shop.id, "Dress Shirt", "Formal white shirt", 8.99, 20);
    }
    
    private void createSampleItem(long shopId, String name, String desc, double price, int stock) {
        Item item = new Item();
        item.id = nextItemId++;
        item.thriftShopId = shopId;
        item.name = name;
        item.description = desc;
        item.price = price;
        item.stockQuantity = stock;
        items.put(item.id, item);
    }
    
    public User findUserByUsername(String username) {
        for (User user : users.values()) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }
}

// 3. CUSTOMER MANAGER
class CustomerManager {
    public DatabaseMock db;
    
    public CustomerManager(DatabaseMock db) {
        this.db = db;
    }
    
    // Authentication
    public User login(String username, String password) {
        User user = db.findUserByUsername(username);
        if (user != null && user.password.equals(password) && user.role == Role.CUSTOMER) {
            return user;
        }
        return null;
    }
    
    // Browsing - Get all items or filter by thrift shop
    public List<Item> browseItems(Long thriftShopId) {
        List<Item> result = new ArrayList<>();
        for (Item item : db.items.values()) {
            if (thriftShopId == null || item.thriftShopId == thriftShopId) {
                result.add(item);
            }
        }
        return result;
    }
    
    // Cart Management
    public void addToCart(long customerId, long itemId, int quantity) {
        Item item = db.items.get(itemId);
        if (item == null || item.stockQuantity < quantity) {
            throw new RuntimeException("Item not available or insufficient stock");
        }
        
        Cart cart = db.carts.computeIfAbsent(customerId, k -> {
            Cart c = new Cart();
            c.customerId = customerId;
            return c;
        });
        
        Optional<CartItem> existing = cart.items.stream()
            .filter(ci -> ci.itemId == itemId)
            .findFirst();
            
        if (existing.isPresent()) {
            existing.get().quantity += quantity;
        } else {
            CartItem cartItem = new CartItem();
            cartItem.itemId = itemId;
            cartItem.quantity = quantity;
            cartItem.price = item.price; // snapshot price
            cart.items.add(cartItem);
        }
    }
    
    public Cart getCart(long customerId) {
        return db.carts.get(customerId);
    }
    
    // Checkout Logic - Calculate total with shipping, tax, discount
    public double calculateCheckoutTotal(Cart cart, String discountType, String billingAddress, String shippingAddress, String paymentMethod) {
        double subtotal = 0;
        for (CartItem ci : cart.items) {
            subtotal += ci.price * ci.quantity;
        }
        
        double shippingCost = 5.0; // Flat shipping
        double taxRate = 0.12; // 12% tax
        double taxAmount = subtotal * taxRate;
        double discountAmount = 0;
        
        // Discounts: PWD/Senior get 10% off
        if ("PWD".equals(discountType) || "SENIOR".equals(discountType)) {
            discountAmount = subtotal * 0.10;
        }
        
        double total = subtotal + shippingCost + taxAmount - discountAmount;
        
        // Create Order
        Order order = new Order();
        order.id = db.nextOrderId++;
        order.customerId = cart.customerId;
        order.thriftShopId = db.items.values().stream()
            .filter(item -> cart.items.stream().anyMatch(ci -> ci.itemId == item.id))
            .findFirst().map(item -> item.thriftShopId).orElse(0L);
        order.items = cart.items.stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.itemId = ci.itemId;
            oi.quantity = ci.quantity;
            oi.price = ci.price;
            return oi;
        }).toList();
        order.billingAddress = billingAddress;
        order.shippingAddress = shippingAddress;
        order.paymentMethod = paymentMethod;
        order.totalAmount = total;
        order.shippingCost = shippingCost;
        order.taxAmount = taxAmount;
        order.discountAmount = discountAmount;
        order.status = OrderStatus.PENDING;
        order.orderDate = LocalDateTime.now();
        
        db.orders.put(order.id, order);
        
        // Deduct stock safely
        for (CartItem ci : cart.items) {
            Item item = db.items.get(ci.itemId);
            if (item != null && item.stockQuantity >= ci.quantity) {
                item.stockQuantity -= ci.quantity;
            } else {
                throw new RuntimeException("Stock deduction failed for item: " + ci.itemId);
            }
        }
        
        // Clear cart
        db.carts.remove(cart.customerId);
        
        return total;
    }
    
    // Order Tracking
    public List<Order> getOrderHistory(long customerId) {
        List<Order> customerOrders = new ArrayList<>();
        for (Order order : db.orders.values()) {
            if (order.customerId == customerId) {
                customerOrders.add(order);
            }
        }
        return customerOrders;
    }
    
    public Order getOrderStatus(long orderId) {
        return db.orders.get(orderId);
    }
}

// 4. ADMIN MANAGER
class AdminManager {
    public DatabaseMock db;
    
    public AdminManager(DatabaseMock db) {
        this.db = db;
    }
    
    // Admin Authentication
    public User login(String username, String password) {
        User user = db.findUserByUsername(username);
        if (user != null && user.password.equals(password) && user.role == Role.ADMIN) {
            return user;
        }
        return null;
    }
    
    // Inventory Management CRUD
    public Item createItem(long thriftShopId, String name, String description, double price, int stockQuantity) {
        Item item = new Item();
        item.id = db.nextItemId++;
        item.thriftShopId = thriftShopId;
        item.name = name;
        item.description = description;
        item.price = price;
        item.stockQuantity = stockQuantity;
        db.items.put(item.id, item);
        logAction(0, "CREATE_ITEM", "Created item: " + name);
        return item;
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(db.items.values());
    }
    
    public Item updateItem(long itemId, String name, String description, double price, int stockQuantity) {
        Item item = db.items.get(itemId);
        if (item != null) {
            item.name = name;
            item.description = description;
            item.price = price;
            item.stockQuantity = stockQuantity;
            logAction(0, "UPDATE_ITEM", "Updated item ID: " + itemId);
        }
        return item;
    }
    
    public boolean deleteItem(long itemId) {
        Item item = db.items.remove(itemId);
        if (item != null) {
            logAction(0, "DELETE_ITEM", "Deleted item ID: " + itemId);
            return true;
        }
        return false;
    }
    
    // Customer Management CRUD
    public List<User> getAllCustomers() {
        List<User> customers = new ArrayList<>();
        for (User user : db.users.values()) {
            if (user.role == Role.CUSTOMER) {
                customers.add(user);
            }
        }
        return customers;
    }
    
    // Order Management
    public List<Order> getAllOrders() {
        return new ArrayList<>(db.orders.values());
    }
    
    public boolean updateOrderStatus(long orderId, OrderStatus status) {
        Order order = db.orders.get(orderId);
        if (order != null) {
            order.status = status;
            logAction(0, "UPDATE_ORDER_STATUS", "Order " + orderId + " status changed to " + status);
            return true;
        }
        return false;
    }
    
    // Audit Logging
    private void logAction(long adminId, String action, String details) {
        AuditLog log = new AuditLog();
        log.id = db.nextAuditLogId++;
        log.adminId = adminId;
        log.action = action;
        log.timestamp = LocalDateTime.now();
        log.details = details;
        db.auditLogs.put(log.id, log);
    }
    
    public List<AuditLog> getAuditLogs() {
        return new ArrayList<>(db.auditLogs.values());
    }
}

// USAGE EXAMPLE (Main method for testing)


public class ThriftShopSystem {
    
    // Make these static so our menu methods can access them
    static DatabaseMock db = new DatabaseMock();
    static CustomerManager customerMgr = new CustomerManager(db);
    static AdminManager adminMgr = new AdminManager(db);
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean systemRunning = true;

        System.out.println("=========================================");
        System.out.println("   Welcome to the Thrift Shop System!    ");
        System.out.println("=========================================");

        while (systemRunning) {
            System.out.println("\n--- LOGIN ---");
            System.out.println("Type 'exit' as username to close the system.");
            System.out.print("Username: ");
            String username = scanner.nextLine();
            
            if (username.equalsIgnoreCase("exit")) {
                systemRunning = false;
                System.out.println("Shutting down system. Goodbye!");
                continue;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine();

            // Try logging in as Customer first, if it fails, try Admin
            User currentUser = customerMgr.login(username, password);
            if (currentUser == null) {
                currentUser = adminMgr.login(username, password);
            }

            if (currentUser != null) {
                System.out.println("\nLogin Successful! Welcome, " + currentUser.username);
                
                // Route to the correct menu based on Role
                if (currentUser.role == Role.CUSTOMER) {
                    runCustomerMenu(currentUser);
                } else if (currentUser.role == Role.ADMIN) {
                    runAdminMenu(currentUser);
                }
            } else {
                System.out.println("Invalid username or password. Please try again.");
            }
        }
        scanner.close();
    }

    // --- CUSTOMER MENU LOGIC ---
    private static void runCustomerMenu(User customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- CUSTOMER MENU ---");
            System.out.println("1. Browse Items");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. View Cart & Checkout");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Item> items = customerMgr.browseItems(null);
                    System.out.println("\n--- AVAILABLE ITEMS ---");
                    for (Item item : items) {
                        System.out.println("ID: " + item.id + " | " + item.name + " | Price: $" + item.price + " | Stock: " + item.stockQuantity);
                    }
                    break;
                case "2":
                    System.out.print("Enter the ID of the item to add: ");
                    long itemId = Long.parseLong(scanner.nextLine());
                    System.out.print("Enter quantity: ");
                    int qty = Integer.parseInt(scanner.nextLine());
                    
                    try {
                        customerMgr.addToCart(customer.id, itemId, qty);
                        System.out.println("Item added to cart successfully!");
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "3":
                    Cart cart = customerMgr.getCart(customer.id);
                    if (cart == null || cart.items.isEmpty()) {
                        System.out.println("Your cart is empty.");
                    } else {
                        System.out.println("\n--- CHECKOUT ---");
                        System.out.println("You have " + cart.items.size() + " unique item(s) in your cart.");
                        System.out.print("Enter Discount Type (NONE, PWD, SENIOR): ");
                        String discount = scanner.nextLine().toUpperCase();
                        System.out.print("Enter Billing Address: ");
                        String billing = scanner.nextLine();
                        System.out.print("Enter Shipping Address: ");
                        String shipping = scanner.nextLine();
                        System.out.print("Enter Payment Method (CASH/CARD): ");
                        String payment = scanner.nextLine();

                        try {
                            double total = customerMgr.calculateCheckoutTotal(cart, discount, billing, shipping, payment);
                            System.out.println("Checkout successful! Total amount paid: $" + total);
                        } catch (Exception e) {
                            System.out.println("Checkout failed: " + e.getMessage());
                        }
                    }
                    break;
                case "4":
                    loggedIn = false;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    // --- ADMIN MENU LOGIC ---
    private static void runAdminMenu(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. View All Items");
            System.out.println("2. Add New Item");
            System.out.println("3. View Audit Logs");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Item> items = adminMgr.getAllItems();
                    System.out.println("\n--- INVENTORY ---");
                    for (Item item : items) {
                        System.out.println("ID: " + item.id + " | " + item.name + " | Stock: " + item.stockQuantity);
                    }
                    break;
                case "2":
                    System.out.print("Enter Item Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Description: ");
                    String desc = scanner.nextLine();
                    System.out.print("Enter Price: ");
                    double price = Double.parseDouble(scanner.nextLine());
                    System.out.print("Enter Initial Stock Quantity: ");
                    int stock = Integer.parseInt(scanner.nextLine());
                    
                    // Assuming shop ID 1 for now
                    adminMgr.createItem(1, name, desc, price, stock); 
                    System.out.println("Item added successfully!");
                    break;
                case "3":
                    List<AuditLog> logs = adminMgr.getAuditLogs();
                    System.out.println("\n--- AUDIT LOGS ---");
                    for (AuditLog log : logs) {
                        System.out.println("[" + log.timestamp + "] " + log.action + " : " + log.details);
                    }
                    break;
                case "4":
                    loggedIn = false;
                    System.out.println("Logging out...");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}