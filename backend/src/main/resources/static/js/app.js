$(function(){
  let currentUserId = null;
  let currentUserRole = null;

  // Render a compact admin detail card for seller/product/user
  function renderAdminCard(type, data) {
    if(!type || !data){
      $('#adminDetailCard').hide();
      return;
    }
    let html = '';
    if(type === 'seller' || type === 'user'){
      html += '<div class="card-row">';
      html += '<div class="card-col"><strong>' + (data.fullName || data.email || 'Unknown') + '</strong><div class="muted">ID: ' + (data.userId || data.id || '') + '</div></div>';
      html += '<div class="card-col">Role: ' + (data.role || '') + '<br/>Verified: ' + (data.verificationStatus || 'N/A') + '</div>';
      html += '<div class="card-col">Active: ' + (data.isActive ? 'Yes' : 'No') + '<br/>Shadow Banned: ' + (data.isShadowBanned ? 'Yes' : 'No') + '</div>';
      html += '</div>';
      if(type === 'seller' && data.productsHtml){
        html += '<hr/>' + data.productsHtml;
      }
    } else if(type === 'product'){
      html += '<div class="card-row">';
      html += '<div class="card-col"><strong>' + (data.itemName || data.name || 'Product') + '</strong><div class="muted">ID: ' + (data.productId || data.id || '') + '</div></div>';
      html += '<div class="card-col">Price: ₱' + (data.price || '0') + '<br/>Status: ' + (data.status || '') + '</div>';
      html += '<div class="card-col">Category: ' + (data.category || '') + '<br/>Seller: ' + ((data.seller && (data.seller.fullName || data.seller.email)) || 'N/A') + '</div>';
      html += '</div>';
      if(data.description){ html += '<hr/><div>' + (data.description || '') + '</div>'; }
    }

    $('#adminCardBody').html(html);
    $('#adminDetailCard').show().data('type', type).data('id', (data.userId||data.productId||data.id||'')).removeClass('muted');
  }

  function setLoggedIn(user){
    currentUserId = user ? user.userId || user.userID || user.id : null;
    currentUserRole = user ? user.role : null;
    if(currentUserId){
      $('#loginStatus').text('Logged in as ' + (user.fullName || user.email) + ' (' + (currentUserRole || '') + ')');
      $('#main-app').show();
      $('.login-wrapper').hide();
      $('#register-section').addClass('hidden-section').hide();
      loadProducts();
      loadCart();
      loadOrders();
    } else {
      $('#loginStatus').text('Not logged in');
      $('#main-app').hide();
      $('.login-wrapper').show();
      $('#products').text('Please login to see products.');
      $('#cart').text('Not logged in.');
      $('#orders').text('Not logged in.');
    }

    if(currentUserRole === 'admin'){
      $('#admin-section').show();
      $('#adminStatus').text('Admin controls are active.');
      loadAdminProducts();
    } else {
      $('#admin-section').hide();
      $('#adminStatus').text('');
    }

    if(currentUserRole === 'seller'){
      $('#seller-section').show();
      $('#seller-request-section').show();
      loadSellerRequests();
    } else {
      $('#seller-section').hide();
      $('#seller-request-section').hide();
    }
  }

  $('#showRegisterLink').on('click', function(e){
    e.preventDefault();
    $('.login-wrapper').hide();
    $('#main-app').show();
    $('#register-section').removeClass('hidden-section').show();
  });

  $('#backToLoginLink').on('click', function(e){
    e.preventDefault();
    $('#register-section').addClass('hidden-section').hide();
    $('.login-wrapper').show();
    $('#main-app').hide();
  });

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
    $.ajax({
      url: '/api/auth/login',
      method: 'POST',
      data: { email: email, password: password }
    })
      .done(function(user){ setLoggedIn(user); })
      .fail(function(xhr){ $('#loginStatus').text('Login failed: ' + xhr.responseText); });
  });

  // --- Register ---
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

  $('#adminLink').on('click', function(e){
    e.preventDefault();
    $('.login-wrapper').hide();
    $('#main-app').show();
    $('#admin-section').show();
    $('#register-section').addClass('hidden-section').hide();
    loadAdminProducts();
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

  // --- Seller product form ---
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

  // --- Seller requests ---
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
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + (currentUserId || 1),
      method: 'PUT'
    })
      .done(function(){ loadAdminProducts(); $('#moderateProductStatus').text('Product moderated successfully.'); loadProductDetails(); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  }

  function loadSellerDetails(){
    const sellerId = $('#adminSellerId').val();
    if(!sellerId){ $('#sellerDetails').text('Seller ID is required.'); return; }
    $('#sellerDetails').text('Loading seller details…');
    $.get('/api/profile/' + sellerId)
      .done(function(user){
        const details = [];
        details.push('Name: ' + (user.fullName || 'N/A'));
        details.push('Email: ' + (user.email || 'N/A'));
        details.push('Role: ' + (user.role || 'N/A'));
        details.push('Verified: ' + (user.verificationStatus || 'N/A'));
        details.push('Approved Seller: ' + (user.sellerApproved ? 'Yes' : 'No'));
        details.push('Active: ' + (user.isActive ? 'Yes' : 'No'));
        details.push('Shadow Banned: ' + (user.isShadowBanned ? 'Yes' : 'No'));
        $('#sellerDetails').html(details.join('<br>'));
        renderAdminCard('seller', user);
        loadSellerProducts(sellerId);
      })
      .fail(function(xhr){ $('#sellerDetails').text('Failed to load seller details: ' + xhr.responseText); });
  }

  function loadSellerProducts(sellerId){
    $('#sellerProducts').text('Loading seller products…');
    $.get('/api/products/seller/' + sellerId)
      .done(function(products){
        if(!products || products.length === 0){ $('#sellerProducts').text('No products found for seller.'); return; }
        const $list = $('<div>');
        products.forEach(p => {
          $list.append($('<div>').html('<strong>' + p.itemName + '</strong> — ' + (p.price || '0') + ' (' + (p.status || 'N/A') + ')'));
        });
        $('#sellerProducts').empty().append($list);
        // If admin card currently shows this seller, append a compact product list
        if($('#adminDetailCard').data('type') === 'seller' && String($('#adminDetailCard').data('id')) === String(sellerId)){
          let productsHtml = '<div><strong>Products</strong><ul>' + products.map(p => '<li>' + (p.itemName || '') + ' — ₱' + (p.price||'0') + ' (' + (p.status||'') + ')</li>').join('') + '</ul></div>';
          const existing = $('#adminCardBody').html() || '';
          $('#adminCardBody').html(existing + '<hr/>' + productsHtml);
        }
      })
      .fail(function(xhr){ $('#sellerProducts').text('Failed to load seller products: ' + xhr.responseText); });
  }

  function loadShadowBanUserDetails(){
    const userId = $('#shadowBanUserId').val();
    if(!userId){ $('#shadowBanUserDetails').text('User ID is required.'); return; }
    $('#shadowBanUserDetails').text('Loading user details…');
    $.get('/api/profile/' + userId)
      .done(function(user){
        const details = [];
        details.push('Name: ' + (user.fullName || 'N/A'));
        details.push('Email: ' + (user.email || 'N/A'));
        details.push('Role: ' + (user.role || 'N/A'));
        details.push('Active: ' + (user.isActive ? 'Yes' : 'No'));
        details.push('Shadow Banned: ' + (user.isShadowBanned ? 'Yes' : 'No'));
        $('#shadowBanUserDetails').html(details.join('<br>'));
        renderAdminCard('user', user);
      })
      .fail(function(xhr){ $('#shadowBanUserDetails').text('Failed to load user details: ' + xhr.responseText); });
  }

  function loadProductDetails(){
    const productId = $('#adminProductId').val();
    if(!productId){ $('#moderationProductDetails').text('Product ID is required.'); return; }
    $('#moderationProductDetails').text('Loading product details…');
    $.get('/api/products/' + productId)
      .done(function(product){
        const details = [];
        details.push('Name: ' + (product.itemName || 'N/A'));
        details.push('Category: ' + (product.category || 'N/A'));
        details.push('Price: ' + (product.price || 'N/A'));
        details.push('Status: ' + (product.status || 'N/A'));
        details.push('Seller: ' + ((product.seller && product.seller.fullName) || (product.seller && product.seller.email) || 'N/A'));
        details.push('Description: ' + (product.description || 'N/A'));
        $('#moderationProductDetails').html(details.join('<br>'));
        renderAdminCard('product', product);
      })
      .fail(function(xhr){ $('#moderationProductDetails').text('Failed to load product details: ' + xhr.responseText); });
  }

  $('#verifySellerForm').on('submit', function(e){
    e.preventDefault();
    const sellerId = $('#adminSellerId').val();
    const status = $('#adminSellerStatus').val();
    const adminId = currentUserId || 1;
    $('#verifySellerStatus').text('Verifying…');
    $.ajax({
      url: '/api/admin/sellers/' + sellerId + '/verify?status=' + status + '&adminId=' + adminId,
      method: 'PUT'
    })
      .done(function(){ $('#verifySellerStatus').text('Seller verification updated.'); })
      .fail(function(xhr){ $('#verifySellerStatus').text('Verify failed: ' + xhr.responseText); });
  });

  $('#changeAccountStatusForm').on('submit', function(e){
    e.preventDefault();
    const userId = $('#adminUserId').val();
    const isActive = $('#adminUserActive').val();
    const adminId = currentUserId || 1;
    $('#changeAccountStatusStatus').text('Updating…');
    $.ajax({
      url: '/api/admin/users/' + userId + '/status?isActive=' + isActive + '&adminId=' + adminId,
      method: 'PUT'
    })
      .done(function(){ $('#changeAccountStatusStatus').text('Account status updated.'); })
      .fail(function(xhr){ $('#changeAccountStatusStatus').text('Update failed: ' + xhr.responseText); });
  });

  $('#shadowBanForm').on('submit', function(e){
    e.preventDefault();
    const userId = $('#shadowBanUserId').val();
    const shadowBanned = $('#adminUserShadowBan').val();
    const adminId = currentUserId || 1;
    $('#shadowBanStatus').text('Updating…');
    $.ajax({
      url: '/api/admin/users/' + userId + '/shadow-ban?shadowBanned=' + shadowBanned + '&adminId=' + adminId,
      method: 'PUT'
    })
      .done(function(){ $('#shadowBanStatus').text('Shadow ban status updated.'); loadShadowBanUserDetails(); })
      .fail(function(xhr){ $('#shadowBanStatus').text('Update failed: ' + xhr.responseText); });
  });

  $('#loadSellerDetailsBtn').on('click', function(){
    loadSellerDetails();
  });

  $('#loadShadowBanUserBtn').on('click', function(){
    loadShadowBanUserDetails();
  });

  $('#loadProductDetailsBtn').on('click', function(){
    loadProductDetails();
  });

  $('#moderateProductForm').on('submit', function(e){
    e.preventDefault();
    const productId = $('#adminProductId').val();
    const status = $('#adminProductStatus').val();
    const adminId = currentUserId || 1;
    $('#moderateProductStatus').text('Moderating…');
    $.ajax({
      url: '/api/admin/products/' + productId + '/moderate?status=' + status + '&adminId=' + adminId,
      method: 'PUT'
    })
      .done(function(){ $('#moderateProductStatus').text('Product moderated successfully.'); loadProducts(); })
      .fail(function(xhr){ $('#moderateProductStatus').text('Moderation failed: ' + xhr.responseText); });
  });

  // Start state
  $('#products').text('Please login to see products.');
});
