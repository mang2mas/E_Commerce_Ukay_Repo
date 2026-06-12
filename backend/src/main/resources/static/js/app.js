$(function(){
  const userId = 1; // change as needed

  function renderOrders(data){
    const $container = $('#orders').empty();
    if(!data || data.length === 0){
      $container.text('No orders found.');
      return;
    }
    data.forEach(o => {
      const $card = $('<div>').addClass('order');
      $card.append($('<div>').text('Order #' + (o.id || 'N/A') + ' — Status: ' + (o.status || 'N/A')));
      $card.append($('<div>').text('Shipping: ' + (o.shippingAddress || '')));
      const $btn = $('<button>').text('Mark Delivered').click(function(){
        $.ajax({
          url: '/api/orders/' + o.id + '/status?status=DELIVERED',
          type: 'PUT',
          success: function(){ loadOrders(); },
          error: function(xhr){ alert('Error: ' + xhr.responseText); }
        });
      });
      $card.append($btn);
      $container.append($card);
    });
  }

  function loadOrders(){
    $('#orders').text('Loading orders…');
    $.get('/api/orders/customer/' + userId)
      .done(function(data){ renderOrders(data); })
      .fail(function(xhr){ $('#orders').text('Failed to load orders: ' + xhr.responseText); });
  }

  $('#checkoutForm').on('submit', function(e){
    e.preventDefault();
    const payload = {
      shippingAddress: $('#shippingAddress').val(),
      paymentMethod: $('#paymentMethod').val()
    };
    $.ajax({
      url: '/api/orders/checkout/' + userId,
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(payload),
      success: function(){ alert('Order placed'); $('#checkoutForm')[0].reset(); loadOrders(); },
      error: function(xhr){ alert('Checkout failed: ' + xhr.responseText); }
    });
  });

  loadOrders();
});
