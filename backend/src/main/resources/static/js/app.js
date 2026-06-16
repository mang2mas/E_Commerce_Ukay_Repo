$(function(){
  let currentUserId = null;
  let currentUserRole = null;

  function hideAllSections(){
    $('#register-section').hide();
    $('#products-section').hide();
    $('#cart-section').hide();
    $('#orders-section').hide();
    $('#checkout-section').hide();
    $('#admin-section').hide();
    $('#seller-section').hide();
    $('#seller-request-section').hide();
  }

  function setLoggedIn(user){
    currentUserId = user ? user.userId || user.userID || user.id : null;
    currentUserRole = user ? user.role : null;
    $('#loginStatus').text(currentUserId ? 'Logged in as ' + (user.fullName || user.email) + ' (' + (currentUserRole || '') + ')' : 'Not logged in');
    
    hideAllSections();
    $('#login-section').hide();
    $('#products-section').show();
    $('#cart-section').show();
    $('#orders-section').show();
    $('#checkout-section').show();
    
    if(currentUserId){ loadProducts(); loadCart(); loadOrders(); }
    else { $('#products').text('Please login to see products.'); $('#cart').text('Not logged in.'); $('#orders').text('Not logged in.'); }

    if(currentUserRole === 'admin'){
      $('#admin-section').show();
      $('#adminStatus').text('Admin controls are active.');
      loadAdminProducts();
    }

    if(currentUserRole === 'seller'){
      $('#seller-section').show();
      $('#seller-request-section').show();
      loadSellerRequests();
    }
  }

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

  $('.signup-link').on('click', function(e){
    e.preventDefault();
    hideAllSections();
    $('#register-section').show();
  });

  $('.admin-link').on('click', function(e){
    e.preventDefault();
    hideAllSections();
    $('#login-section').show();
    $('#email').val('');
    $('#password').val('');
    $('#loginStatus').text('Please log in as admin');
  });

  $('#loginForm').on('submit', function(e){
    e.preventDefault();
    const email = $('#email').val();
    const password = $('#password').val();
    $('#loginStatus').text('Logging in…');
    $.ajax({
      url: '/api/auth/login',
      method: 'POST',
      data: { email: email, password: password }
    })
      .done(function(user){ setLoggedIn(user); })
      .fail(function(xhr){ $('#loginStatus').text('Login failed: ' + xhr.responseText); });
  });

  $('#registerForm').on('submit', function(e){
    e.preventDefault();
    const payload = {
      fullName: $('#registerName').val(),
      email: $('#registerEmail').val(),
      password: $('#registerPassword').val(),
      role: $('#registerRole').val()
    };
    $('#registerStatus').text('Registering…');
    $.ajax({
      url: '/api/auth/register',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
      .done(function(user){
        $('#registerStatus').text('Registration successful! You can now log in.');
        setTimeout(() => {
          hideAllSections();
          $('#login-section').show();
          $('#email').val(payload.email);
          $('#password').val('');
          $('#registerForm')[0].reset();
        }, 1500);
      })
      .fail(function(xhr){
        $('#registerStatus').text('Register failed: ' + xhr.responseText);
      });
  });

  $('#checkoutForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login to checkout'); return; }
    const payload = { shippingAddress: $('#shippingAddress').val(), paymentMethod: $('#paymentMethod').val() };
    $.ajax({ url: '/api/orders/checkout/' + currentUserId, method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(){ alert('Order placed'); $('#checkoutForm')[0].reset(); loadOrders(); loadCart(); })
      .fail(function(xhr){ alert('Checkout failed: ' + xhr.responseText); });
  });

  $('#sellerProductForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login first'); return; }

    const payload = {
      itemName: $('#productName').val(),
      category: $('#productCategory').val(),
      price: parseFloat($('#productPrice').val()),
      stockQuantity: parseInt($('#productStock').val(), 10),
      size: $('#productSize').val(),
      itemCondition: $('#productCondition').val(),
      imagePath: $('#productImagePath').val(),
      description: $('#productDescription').val()
    };

    $.ajax({
      url: '/api/products/seller/' + currentUserId,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
      .done(function(){
        $('#sellerProductStatus').text('Product added successfully.');
        $('#sellerProductForm')[0].reset();
        loadProducts();
      })
      .fail(function(xhr){
        $('#sellerProductStatus').text('Add product failed: ' + xhr.responseText);
      });
  });

  function renderSellerRequests(list){
    const $c = $('#sellerRequests').empty();
    if(!list || list.length === 0){ $c.text('No requests found.'); return; }
    list.forEach(r => {
      const $card = $('<div>').addClass('request');
      $card.append($('<div>').text('Request #' + (r.requestId || r.id || 'N/A') + ' — Status: ' + (r.status || 'PENDING')));
      $card.append($('<div>').text('Business: ' + (r.businessName || '')));
      $card.append($('<div>').text('Description: ' + (r.businessDescription || '')));
      $card.append($('<div>').text('Permit: ' + (r.businessPermit || '')));
      $card.append($('<div>').text('Notes: ' + (r.notes || '')));
      $c.append($card);
    });
  }

  function loadSellerRequests(){
    if(!currentUserId){ $('#sellerRequests').text('Not logged in.'); return; }
    $('#sellerRequests').text('Loading requests…');
    $.get('/api/seller/requests?userId=' + currentUserId)
      .done(renderSellerRequests)
      .fail(function(xhr){ $('#sellerRequests').text('Failed to load requests: ' + xhr.responseText); });
  }

  $('#sellerRequestForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login as a seller'); return; }
    const payload = {
      userId: currentUserId,
      businessName: $('#bizName').val(),
      businessDescription: $('#bizDescription').val(),
      businessPermit: $('#bizPermit').val(),
      notes: $('#bizNotes').val()
    };
    $('#sellerRequestStatus').text('Submitting…');
    $.ajax({ url: '/api/seller/request', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(){ $('#sellerRequestStatus').text('Submitted.'); $('#sellerRequestForm')[0].reset(); loadSellerRequests(); })
      .fail(function(xhr){ $('#sellerRequestStatus').text('Submit failed: ' + xhr.responseText); });
  });

  function renderAdminProducts(list){
    const $c = $('#adminProducts').empty();
    if(!list || list.length === 0){ $c.text('No products available.'); return; }
    list.forEach(p => {
      const $card = $('<div>').addClass('product admin-product');
      const sellerName = p.seller?.fullName || p.seller?.email || 'Unknown seller';
      $card.append($('<div>').html('<strong>' + p.itemName + '</strong> — ₱' + (p.price || '0')));
      $card.append($('<div>').text('Status: ' + (p.status || 'N/A')));
      $card.append($('<div>').text('Seller: ' + sellerName));
      $card.append($('<div>').text('Category: ' + (p.category || 'N/A')));
      $card.append($('<div>').text('Stock: ' + (p.stockQuantity || 0)));

      const $select = $('<select>').addClass('admin-moderate-select');
      ['AVAILABLE','REJECTED','PENDING_APPROVAL'].forEach(status => {
        const $opt = $('<option>').attr('value', status).text(status);
        if(status === p.status){ $opt.prop('selected', true); }
        $select.append($opt);
      });
      const $button = $('<button>').text('Update status').click(function(){
        moderateAdminProduct(p.productId, $select.val());
      });
      $card.append($('<div>').append('Set status: ', $select, ' ', $button));
      $c.append($card);
    });
  }

  function loadAdminProducts(){
    $('#adminProducts').text('Loading admin products…');
    $.get('/api/products/admin')
      .done(renderAdminProducts)
      .fail(function(xhr){ $('#adminProducts').text('Failed to load admin products: ' + xhr.responseText); });
  }

  function moderateAdminProduct(productId, status){
    $.ajax({
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ loadAdminProducts(); $('#moderateProductStatus').text('Product moderated successfully.'); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  }

  $('#verifySellerForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const sellerId = $('#adminSellerId').val();
    const status = $('#adminSellerStatus').val();
    $('#verifySellerStatus').text('Verifying…');
    $.ajax({
      url: '/api/admin/sellers/' + sellerId + '/verify?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#verifySellerStatus').text('Seller verification updated.'); })
      .fail(function(xhr){ $('#verifySellerStatus').text('Verify failed: ' + xhr.responseText); });
  });

  $('#changeAccountStatusForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const userId = $('#adminUserId').val();
    const isActive = $('#adminUserActive').val();
    $('#changeAccountStatusStatus').text('Updating…');
    $.ajax({
      url: '/api/admin/users/' + userId + '/status?isActive=' + isActive + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#changeAccountStatusStatus').text('Account status updated.'); })
      .fail(function(xhr){ $('#changeAccountStatusStatus').text('Update failed: ' + xhr.responseText); });
  });

  $('#moderateProductForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const productId = $('#adminProductId').val();
    const status = $('#adminProductStatus').val();
    $('#moderateProductStatus').text('Moderating…');
    $.ajax({
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#moderateProductStatus').text('Product moderated successfully.'); loadProducts(); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  });

  hideAllSections();
  $('#login-section').show();
});

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

  $('#loginForm').on('submit', function(e){
    e.preventDefault();
    const email = $('#email').val();
    const password = $('#password').val();
    $('#loginStatus').text('Logging in…');
    $.ajax({
      url: '/api/auth/login',
      method: 'POST',
      data: { email: email, password: password }
    })
      .done(function(user){ setLoggedIn(user); })
      .fail(function(xhr){ $('#loginStatus').text('Login failed: ' + xhr.responseText); });
  });

  $('#registerForm').on('submit', function(e){
    e.preventDefault();
    const payload = {
      fullName: $('#registerName').val(),
      email: $('#registerEmail').val(),
      password: $('#registerPassword').val(),
      role: $('#registerRole').val()
    };
    $('#registerStatus').text('Registering…');
    $.ajax({
      url: '/api/auth/register',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
      .done(function(user){
        $('#registerStatus').text('Registration successful! You can now log in.');
      })
      .fail(function(xhr){
        $('#registerStatus').text('Register failed: ' + xhr.responseText);
      });
  });

  $('#checkoutForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login to checkout'); return; }
    const payload = { shippingAddress: $('#shippingAddress').val(), paymentMethod: $('#paymentMethod').val() };
    $.ajax({ url: '/api/orders/checkout/' + currentUserId, method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(){ alert('Order placed'); $('#checkoutForm')[0].reset(); loadOrders(); loadCart(); })
      .fail(function(xhr){ alert('Checkout failed: ' + xhr.responseText); });
  });

  function setSellerSection(user){
    if(user && user.role === 'seller'){
      $('#seller-section').show();
      const approved = user.sellerApproved === true;
      $('#sellerStatus').text(approved ? 'Seller is approved and can add products.' : 'Seller is not approved yet.');
      if(approved){ $('#sellerProductForm').show(); }
      else { $('#sellerProductForm').hide(); }
    } else {
      $('#seller-section').hide();
    }
  }

  $('#sellerProductForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login first'); return; }

    const payload = {
      itemName: $('#productName').val(),
      category: $('#productCategory').val(),
      price: parseFloat($('#productPrice').val()),
      stockQuantity: parseInt($('#productStock').val(), 10),
      size: $('#productSize').val(),
      itemCondition: $('#productCondition').val(),
      imagePath: $('#productImagePath').val(),
      description: $('#productDescription').val()
    };

    $.ajax({
      url: '/api/products/seller/' + currentUserId,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
      .done(function(){
        $('#sellerProductStatus').text('Product added successfully.');
        $('#sellerProductForm')[0].reset();
        loadProducts();
      })
      .fail(function(xhr){
        $('#sellerProductStatus').text('Add product failed: ' + xhr.responseText);
      });
  });

  function renderSellerRequests(list){
    const $c = $('#sellerRequests').empty();
    if(!list || list.length === 0){ $c.text('No requests found.'); return; }
    list.forEach(r => {
      const $card = $('<div>').addClass('request');
      $card.append($('<div>').text('Request #' + (r.requestId || r.id || 'N/A') + ' — Status: ' + (r.status || 'PENDING')));
      $card.append($('<div>').text('Business: ' + (r.businessName || '')));
      $card.append($('<div>').text('Description: ' + (r.businessDescription || '')));
      $card.append($('<div>').text('Permit: ' + (r.businessPermit || '')));
      $card.append($('<div>').text('Notes: ' + (r.notes || '')));
      $c.append($card);
    });
  }

  function loadSellerRequests(){
    if(!currentUserId){ $('#sellerRequests').text('Not logged in.'); return; }
    $('#sellerRequests').text('Loading requests…');
    $.get('/api/seller/requests?userId=' + currentUserId)
      .done(renderSellerRequests)
      .fail(function(xhr){ $('#sellerRequests').text('Failed to load requests: ' + xhr.responseText); });
  }

  $('#sellerRequestForm').on('submit', function(e){
    e.preventDefault();
    if(!currentUserId){ alert('Please login as a seller'); return; }
    const payload = {
      userId: currentUserId,
      businessName: $('#bizName').val(),
      businessDescription: $('#bizDescription').val(),
      businessPermit: $('#bizPermit').val(),
      notes: $('#bizNotes').val()
    };
    $('#sellerRequestStatus').text('Submitting…');
    $.ajax({ url: '/api/seller/request', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
      .done(function(){ $('#sellerRequestStatus').text('Submitted.'); $('#sellerRequestForm')[0].reset(); loadSellerRequests(); })
      .fail(function(xhr){ $('#sellerRequestStatus').text('Submit failed: ' + xhr.responseText); });
  });

  function renderAdminProducts(list){
    const $c = $('#adminProducts').empty();
    if(!list || list.length === 0){ $c.text('No products available.'); return; }
    list.forEach(p => {
      const $card = $('<div>').addClass('product admin-product');
      const sellerName = p.seller?.fullName || p.seller?.email || 'Unknown seller';
      $card.append($('<div>').html('<strong>' + p.itemName + '</strong> — ₱' + (p.price || '0')));
      $card.append($('<div>').text('Status: ' + (p.status || 'N/A')));
      $card.append($('<div>').text('Seller: ' + sellerName));
      $card.append($('<div>').text('Category: ' + (p.category || 'N/A')));
      $card.append($('<div>').text('Stock: ' + (p.stockQuantity || 0)));

      const $select = $('<select>').addClass('admin-moderate-select');
      ['AVAILABLE','REJECTED','PENDING_APPROVAL'].forEach(status => {
        const $opt = $('<option>').attr('value', status).text(status);
        if(status === p.status){ $opt.prop('selected', true); }
        $select.append($opt);
      });
      const $button = $('<button>').text('Update status').click(function(){
        moderateAdminProduct(p.productId, $select.val());
      });
      $card.append($('<div>').append('Set status: ', $select, ' ', $button));
      $c.append($card);
    });
  }

  function loadAdminProducts(){
    $('#adminProducts').text('Loading admin products…');
    $.get('/api/products/admin')
      .done(renderAdminProducts)
      .fail(function(xhr){ $('#adminProducts').text('Failed to load admin products: ' + xhr.responseText); });
  }

  function moderateAdminProduct(productId, status){
    $.ajax({
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ loadAdminProducts(); $('#moderateProductStatus').text('Product moderated successfully.'); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  }

  $('#verifySellerForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const sellerId = $('#adminSellerId').val();
    const status = $('#adminSellerStatus').val();
    $('#verifySellerStatus').text('Verifying…');
    $.ajax({
      url: '/api/admin/sellers/' + sellerId + '/verify?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#verifySellerStatus').text('Seller verification updated.'); })
      .fail(function(xhr){ $('#verifySellerStatus').text('Verify failed: ' + xhr.responseText); });
  });

  $('#changeAccountStatusForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const userId = $('#adminUserId').val();
    const isActive = $('#adminUserActive').val();
    $('#changeAccountStatusStatus').text('Updating…');
    $.ajax({
      url: '/api/admin/users/' + userId + '/status?isActive=' + isActive + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#changeAccountStatusStatus').text('Account status updated.'); })
      .fail(function(xhr){ $('#changeAccountStatusStatus').text('Update failed: ' + xhr.responseText); });
  });

  $('#moderateProductForm').on('submit', function(e){
    e.preventDefault();
    if(currentUserRole !== 'admin'){ alert('Admin login required'); return; }
    const productId = $('#adminProductId').val();
    const status = $('#adminProductStatus').val();
    $('#moderateProductStatus').text('Moderating…');
    $.ajax({
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + currentUserId,
      method: 'PUT'
    })
      .done(function(){ $('#moderateProductStatus').text('Product moderated successfully.'); loadProducts(); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  });

  $('#products').text('Please login to see products.');
});
