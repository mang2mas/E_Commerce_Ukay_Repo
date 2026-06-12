$(function(){
  let currentUserId = null;

  function setLoggedIn(user){
    currentUserId = user ? user.userId || user.userID || user.id : null;
    $('#loginStatus').text(currentUserId ? 'Logged in as ' + (user.fullName || user.email) : 'Not logged in');
    if(currentUserId){ loadProducts(); loadCart(); loadOrders(); }
    else { $('#products').text('Please login to see products.'); $('#cart').text('Not logged in.'); $('#orders').text('Not logged in.'); }
  }

  // --- Products ---
  function renderProducts(list){
    const $c = $('#products').empty();
    if(!list || list.length === 0){ $c.text('No products available.'); return; }
    list.forEach(p => {
      const $card = $('<div>').addClass('product');
      $card.append($('<div>').text(p.itemName + ' — ₱' + (p.price || '0')));
      const $btn = $('<button>').text('Add to cart').click(function(){
        if(!currentUserId){ alert('Please login first'); return; }
        $.post('/api/cart/add?userId=' + currentUserId + '&productId=' + p.productId)
          .done(function(){ alert('Added to cart'); loadCart(); })
          .fail(function(xhr){ alert('Add failed: ' + xhr.responseText); });
      });
      $card.append($btn);
      $c.append($card);
    });
  }

  function loadProducts(){
    $('#products').text('Loading products…');
    $.get('/api/products')
      .done(renderProducts)
      .fail(function(xhr){ $('#products').text('Failed to load products: ' + xhr.responseText); });
  }

  // --- Cart ---
  function renderCart(items){
    const $c = $('#cart').empty();
    if(!items || items.length === 0){ $c.text('Cart is empty.'); return; }
    items.forEach(it => {
      const $row = $('<div>').addClass('cart-item');
      $row.append($('<div>').text((it.product?.itemName || 'Item') + ' x' + (it.quantity || 1)));
      const $rm = $('<button>').text('Remove').click(function(){
        $.ajax({ url: '/api/cart/' + it.cartId, type: 'DELETE' })
          .done(function(){ loadCart(); })
          .fail(function(xhr){ alert('Remove failed: ' + xhr.responseText); });
      });
      $row.append($rm);
      $c.append($row);
    });
  }

  function loadCart(){
    if(!currentUserId){ $('#cart').text('Not logged in.'); return; }
    $('#cart').text('Loading cart…');
    $.get('/api/cart/user/' + currentUserId)
      .done(renderCart)
      .fail(function(xhr){ $('#cart').text('Failed to load cart: ' + xhr.responseText); });
  }

  // --- Orders ---
  function renderOrders(data){
    const $container = $('#orders').empty();
    if(!data || data.length === 0){ $container.text('No orders found.'); return; }
    data.forEach(o => {
      const $card = $('<div>').addClass('order');
      $card.append($('<div>').text('Order #' + (o.id || o.orderId || 'N/A') + ' — Status: ' + (o.status || 'N/A')));
      $card.append($('<div>').text('Shipping: ' + (o.shippingAddress || '')));
      $container.append($card);
    });
  }

  function loadOrders(){
    if(!currentUserId){ $('#orders').text('Not logged in.'); return; }
    $('#orders').text('Loading orders…');
    $.get('/api/orders/customer/' + currentUserId)
      .done(function(data){ renderOrders(data); })
      .fail(function(xhr){ $('#orders').text('Failed to load orders: ' + xhr.responseText); });
  }

  // --- Login ---
  $('#loginForm').on('submit', function(e){
    e.preventDefault();
    const email = $('#email').val();
    const password = $('#password').val();
    $('#loginStatus').text('Logging in…');
    $.get('/api/auth/login', { email: email, password: password })
      .done(function(user){ setLoggedIn(user); })
      .fail(function(xhr){ $('#loginStatus').text('Login failed: ' + xhr.responseText); });
  });

  // --- Checkout ---
  $('#checkoutForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login to checkout'); return; }
    const payload = { shippingAddress: $('#shippingAddress').val(), paymentMethod: $('#paymentMethod').val() };
    $.ajax({ url: '/api/orders/checkout/' + currentUserId, method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(){ alert('Order placed'); $('#checkoutForm')[0].reset(); loadOrders(); loadCart(); })
      .fail(function(xhr){ alert('Checkout failed: ' + xhr.responseText); });
  });

  // Start state
  $('#products').text('Please login to see products.');
});
