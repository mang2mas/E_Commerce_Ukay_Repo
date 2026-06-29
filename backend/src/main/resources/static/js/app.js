let currentUserId = sessionStorage.getItem("currentUserId") || null;
let currentUserRole = sessionStorage.getItem("currentUserRole") || null;

function getAuthToken() {
    return sessionStorage.getItem("authToken") || null;
}

function getAuthHeaders() {
    const token = getAuthToken();
    return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function authFetch(url, options = {}) {
    const headers = Object.assign({}, options.headers || {}, getAuthHeaders());
    return fetch(url, Object.assign({}, options, { headers }));
}

function setAuthHeader(xhr) {
    const token = getAuthToken();
    if (token) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    }
}

function normalizeImagePath(path) {
    if (!path || typeof path !== 'string') return '/images/item1.jpg';
    const trimmed = path.trim();
    if (trimmed === '') return '/images/item1.jpg';
    if (/^https?:\/\//i.test(trimmed)) return trimmed;
    return trimmed.startsWith('/') ? trimmed : '/' + trimmed;
}

function getProductContext(target) {
    const card = target?.closest('.masonry-item');
    const button = target?.closest('.btn-add-cart-action');
    const selectedProduct = sessionStorage.getItem('selectedProduct');
    let parsedProduct = null;


    if (selectedProduct) {
        try {
            parsedProduct = JSON.parse(selectedProduct);
        } catch (error) {
            console.warn('Unable to parse selectedProduct data', error);
        }
    }


    const productId = card?.dataset.productId || button?.dataset.productId || parsedProduct?.productId || new URLSearchParams(window.location.search).get('productId');
    const productName = card?.querySelector('.item-title-badge')?.textContent?.trim() || button?.dataset.productName || parsedProduct?.productName || 'item';
    const price = card?.querySelector('.item-price-tag')?.textContent?.trim() || parsedProduct?.price;
    const rawImage = card?.querySelector('img')?.src || parsedProduct?.imagePath || parsedProduct?.imageUrl;
    const imageUrl = normalizeImagePath(rawImage);

    return { productId, productName, price, imageUrl };
}


document.addEventListener("DOMContentLoaded", () => {
   
// ==========================================================================
    // 0. GLOBAL GATEWAY INTERCEPTOR (HARD REDIRECT TO LOGIN PAGE)
    // ==========================================================================
    document.body.addEventListener("click", (event) => {
        const itemCard = event.target.closest('.masonry-item');
        const addCartBtn = event.target.closest('.btn-add-cart-action');
        const checkoutBtn = event.target.closest('.btn-checkout');
        const cartClick = event.target.closest('.cart-trigger');
        const profileClick = event.target.closest('.profile-trigger');
        const homeClick = event.target.closest('.home-trigger');
        const protectedTarget = event.target.closest('.profile-trigger, .cart-trigger, .home-trigger, .shop-trigger, .category-card, .view-all, .btn-primary, .btn-outline, .btn-add-cart-action, .btn-checkout');
        const anchor = event.target.closest('a');
       
        // Check session storage directly to be safe
        const isAuthenticated = Boolean(sessionStorage.getItem("currentUserId"));


        if (itemCard || protectedTarget) {
            if (!isAuthenticated) {
                event.preventDefault();
                event.stopPropagation();

                const productContext = getProductContext(event.target);
                const targetProductId = productContext.productId;
                let nextUrl = anchor?.href || window.location.href;
                if (itemCard && targetProductId) {
                    nextUrl = `./ItemDescription.html?productId=${encodeURIComponent(targetProductId)}`;
                } else if (checkoutBtn) {
                    nextUrl = './checkout.html';
                }
                sessionStorage.setItem('pendingRedirect', nextUrl);

                const modal = document.getElementById('loginModalOverlay');
                if (modal) {
                    modal.classList.add('active');
                    document.body.classList.add('modal-open');
                } else {
                    window.location.href = './login.html?next=' + encodeURIComponent(nextUrl);
                }
                return;
            }


            if (cartClick) {
                event.preventDefault();
                event.stopPropagation();
                window.location.href = './shoppingcart.html';
                return;
            }


            if (profileClick) {
                event.preventDefault();
                event.stopPropagation();
                window.location.href = './ordermanagement.html';
                return;
            }


            if (homeClick) {
                event.preventDefault();
                event.stopPropagation();
                window.location.href = './index.html';
                return;
            }


            if (addCartBtn) {
                event.preventDefault();
                event.stopPropagation();
                const { productId, productName } = getProductContext(addCartBtn);


                if (!productId) {
                    alert('Unable to determine product to add to cart.');
                    return;
                }


                authFetch(`/api/cart/add?userId=${currentUserId}&productId=${productId}`, {
                    method: 'POST'
                })
                    .then(response => {
                        if (!response.ok) return response.text().then(text => { throw new Error(text || 'Add to cart failed.'); });
                        return response.json();
                    })
                    .then(() => {
                        alert(`Added ${productName || 'item'} to cart.`);
                    })
                    .catch(error => {
                        alert(error.message || 'Unable to add item to cart.');
                    });
                return;
            }


            if (itemCard) {
                event.preventDefault();
                event.stopPropagation();
                const { productId, productName, price, imageUrl } = getProductContext(itemCard);


                if (!productId) {
                    return;
                }


                const selectedProduct = {
                    productId,
                    productName,
                    price,
                    imageUrl
                };
                sessionStorage.setItem('selectedProduct', JSON.stringify(selectedProduct));
                window.location.href = `./ItemDescription.html?productId=${encodeURIComponent(productId)}`;
                return;
            }


            if (checkoutBtn) {
                event.preventDefault();
                event.stopPropagation();
                window.location.href = './checkout.html';
                return;
            }
        }
    }, true);
    function refreshLoginStatusIndicator() {
        const statusLabel = document.getElementById('loginStatus');
        if (!statusLabel) return;
        statusLabel.textContent = currentUserId ? 'Logged in' : 'Not logged in';
    }


    refreshLoginStatusIndicator();
    loadProducts();


    const navbar = document.querySelector(".navbar");
    if (navbar) {
        const updateNavbarOverlay = () => {
            if (window.scrollY > 50) {
                navbar.classList.add("scrolled");
            } else {
                navbar.classList.remove("scrolled");
            }
        };
        window.addEventListener("scroll", updateNavbarOverlay);
        updateNavbarOverlay();
    }


    // ==========================================================================
    // 1. NAVBAR TOGGLE STATES
    // ==========================================================================
    const toggleButtons = document.querySelectorAll(".toggle-btn");
    toggleButtons.forEach(button => {
        button.addEventListener("click", () => {
            const isSellView = button.dataset.view === 'sell';

            if (isSellView && !currentUserId) {
                const target = './css/SellerRegistration2.html';
                sessionStorage.setItem('pendingRedirect', target);
                const modal = document.getElementById('loginModalOverlay');
                if (modal) {
                    modal.classList.add('active');
                    document.body.classList.add('modal-open');
                } else {
                    window.location.href = `./login.html?next=${encodeURIComponent(target)}`;
                }
                return;
            }

            toggleButtons.forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");

            if (isSellView) {
                const target = './css/SellerRegistration2.html';
                window.location.href = target;
            }
        });
    });


    const loginTrigger = document.querySelector('.login-trigger');
    if (loginTrigger) {
        loginTrigger.addEventListener('click', (event) => {
            event.preventDefault();
            const modal = document.getElementById('loginModalOverlay');
            if (!modal) {
                window.location.href = './login.html';
                return;
            }
            modal.classList.add('active');
            document.body.classList.add('modal-open');
        });
    }


    // ==========================================================================
    // 2. HERO CAROUSEL CONTROLLER
    // ==========================================================================
    const slides = document.querySelectorAll(".slide");
    const prevBtn = document.getElementById("prevSlide");
    const nextBtn = document.getElementById("nextSlide");
    let currentIdx = 0;
    let slideInterval;


    function showSlide(index) {
        if (slides.length === 0) return;
        slides.forEach(slide => slide.classList.remove("active"));
        currentIdx = index;
        if (currentIdx >= slides.length) currentIdx = 0;
        if (currentIdx < 0) currentIdx = slides.length - 1;
        slides[currentIdx].classList.add("active");
    }


    function nextSlide() {
        showSlide(currentIdx + 1);
    }


    function startAutoplay() {
        if (slides.length > 0) {
            slideInterval = setInterval(nextSlide, 5000);
        }
    }


    function resetAutoplay() {
        clearInterval(slideInterval);
        startAutoplay();
    }


    if (nextBtn && prevBtn) {
        nextBtn.addEventListener("click", () => {
            nextSlide();
            resetAutoplay();
        });
        prevBtn.addEventListener("click", () => {
            showSlide(currentIdx - 1);
            resetAutoplay();
        });
    }


    startAutoplay();


    // ==========================================================================
    // 3. TEAM BACKEND LIFECYCLE CHANNELS
    // ==========================================================================
function setLoggedIn(user){
    currentUserId = user ? user.userId || user.userID || user.id : null;
    currentUserRole = user ? user.role : null;
   
    if(currentUserId){
        // Add these two lines here to persist the session!
        sessionStorage.setItem("currentUserId", currentUserId);
        sessionStorage.setItem("currentUserRole", currentUserRole);


        $('#loginStatus').text('Logged in as ' + (user.fullName || user.email) + ' (' + (currentUserRole || '') + ')');
        $('#main-app').show();
        $('.login-wrapper').hide();
        $('#register-section').addClass('hidden-section').hide();
        loadProducts();
        loadCart();
        loadOrders();
        loadProfile();
        loadNotifications();
    } else {
        // Clear them out if they log out
        sessionStorage.removeItem("currentUserId");
        sessionStorage.removeItem("currentUserRole");


        $('#loginStatus').text('Not logged in');
        $('#main-app').hide();
        $('.login-wrapper').show();
        $('#products').text('Please login to see products.');
        $('#cart').text('Not logged in.');
        $('#orders').text('Not logged in.');
    }
}
    function loadCart(){
        if(!currentUserId){
            $('#cart').text('Not logged in.');
            $('#cartItemsContainer').empty();
            $('#cartEmptyState').show();
            $('#cartItemsCount').text('0 items');
            $('#cartEstimatedTotal').text('₱0.00');
            return;
        }

        const cartPageContainer = $('#cartItemsContainer');
        if (cartPageContainer.length) {
            cartPageContainer.empty();
            $('#cartEmptyState').show().text('Loading cart…');
        }
        const legacyCart = $('#cart');
        if (legacyCart.length) {
            legacyCart.text('Loading cart…');
        }

        $.ajax({
            url: '/api/cart/user/' + currentUserId,
            method: 'GET',
            beforeSend: setAuthHeader
        })
            .done(renderCart)
            .fail(function(xhr){
                if (legacyCart.length) {
                    legacyCart.text('Failed to load cart: ' + xhr.responseText);
                }
                if (cartPageContainer.length) {
                    $('#cartEmptyState').show().text('Failed to load cart: ' + xhr.responseText);
                }
            });
    }


    function formatCurrency(value) {
        const amount = Number(value);
        return Number.isFinite(amount) ? `₱${amount.toFixed(2)}` : '₱0.00';
    }

    function renderCart(cartItems) {
        const items = Array.isArray(cartItems) ? cartItems : [];
        const cartContainer = $('#cartItemsContainer');
        const cartEmptyState = $('#cartEmptyState');
        const legacyCart = $('#cart');

        const totalQuantity = items.reduce((sum, item) => sum + (Number(item.quantity) || 0), 0);
        const totalPrice = items.reduce((sum, item) => {
            const productPrice = Number(item.product?.price) || 0;
            return sum + productPrice * (Number(item.quantity) || 1);
        }, 0);

        if (cartContainer.length) {
            cartContainer.empty();
            if (items.length === 0) {
                cartEmptyState.show().text('Your cart is empty.');
            } else {
                cartEmptyState.hide();
                items.forEach(item => {
                    const product = item.product || {};
                    const imageUrl = normalizeImagePath(product.imagePath || product.imageUrl);
                    const quantity = Number(item.quantity) || 1;
                    const unitPrice = Number(product.price) || 0;
                    const subtotal = unitPrice * quantity;

                    const $itemWrapper = $('<div>').addClass('seller-group');
                    const $itemRow = $('<div>').addClass('cart-item');
                    $itemRow.append($('<img>').attr('src', imageUrl).attr('alt', product.itemName || 'Cart item'));

                    const $info = $('<div>').css('flex', '1');
                    $info.append($('<b>').text(product.itemName || 'Untitled item'));
                    $info.append($('<p>').css({'font-size':'12px', 'color':'#666'}).text(`Category: ${product.category || 'N/A'} · Size: ${product.size || 'N/A'}`));
                    $info.append($('<p>').css({'color':'#f47a55', 'font-weight':'700'}).text(formatCurrency(unitPrice)));
                    $itemRow.append($info);

                    const $actions = $('<div>').css({'text-align':'right'});
                    $actions.append($('<div>').css({'margin-bottom':'10px'}).text(`Qty: ${quantity}`));
                    $actions.append($('<div>').css({'margin-bottom':'10px', 'font-weight':'700'}).text(formatCurrency(subtotal)));
                    $actions.append($('<button>').text('Remove').css({'border':'none','background':'none','color':'#f47a55','text-decoration':'underline','cursor':'pointer'})
                        .click(function(event) {
                            event.preventDefault();
                            if (!confirm('Remove this item from your cart?')) return;
                            $.ajax({ url: '/api/cart/' + item.cartId, method: 'DELETE' })
                                .done(function() { loadCart(); })
                                .fail(function(xhr) { alert('Unable to remove item: ' + xhr.responseText); });
                        }));
                    $itemRow.append($actions);
                    $itemWrapper.append($itemRow);
                    cartContainer.append($itemWrapper);
                });
            }
            $('#cartItemsCount').text(`${totalQuantity} item${totalQuantity === 1 ? '' : 's'}`);
            $('#cartEstimatedTotal').text(formatCurrency(totalPrice));
        }

        if (legacyCart.length) {
            if (items.length === 0) {
                legacyCart.text('Your cart is empty.');
            } else {
                legacyCart.empty();
                const summary = $('<div>').text(`Cart (${totalQuantity} item${totalQuantity === 1 ? '' : 's'}) - ${formatCurrency(totalPrice)}`);
                legacyCart.append(summary);
                items.slice(0, 3).forEach(item => {
                    const product = item.product || {};
                    legacyCart.append($('<div>').text(`${product.itemName || 'Item'} x${item.quantity || 1}`));
                });
                if (items.length > 3) {
                    legacyCart.append($('<div>').text(`+ ${items.length - 3} more items`));
                }
            }
        }
    }

    function renderOrders(data){
        const $container = $('#orders').empty();
        if(!data || data.length === 0){ $container.text('No orders found.'); return; }
        data.forEach(o => {
            const orderId = o.orderId || o.id || 'N/A';
            const status = o.orderStatus || o.status || 'N/A';
            const $card = $('<div>').addClass('order');
            $card.append($('<div>').html('<strong>Order #' + orderId + '</strong> — Status: ' + status));
            $card.append($('<div>').text('Shipping: ' + (o.shippingAddress || '')));
            $card.append($('<div>').text('Payment: ' + (o.paymentMethod || '')));
            $card.append($('<div>').text('Total: ₱' + ((o.totalAmount != null) ? o.totalAmount : '0')));
            const $shipmentArea = $('<div>').addClass('order-shipments').text('Loading shipment status...');
            const $itemsArea = $('<div>').addClass('order-items').text('Loading purchased items...');
            $card.append($itemsArea);
            $card.append($shipmentArea);
            $container.append($card);
            if(orderId !== 'N/A'){
                loadOrderItems(orderId, $itemsArea);
                loadShipments(orderId, $shipmentArea);
            }
        });
    }


    function loadProducts() {
        const container = $('#newArrivalsContainer');
        if (!container.length) {
            console.warn('New arrivals container not found');
            return;
        }

        const userId = currentUserId ? Number(currentUserId) : null;
        container.html('<div class="recommended-loading">Loading new arrivals…</div>');
        const query = userId ? `?userId=${userId}` : '';

        $.get(`/api/products/recommended${query}`)
            .done(function(products) {
                renderRecommendedProducts(products, '#newArrivalsContainer');
            })
            .fail(function(xhr) {
                console.error('Failed to load recommended products:', xhr.responseText);
                container.html('<div class="recommended-loading">Unable to load recommendations.</div>');
            });
    }

    function renderRecommendedProducts(products, containerSelector = '#newArrivalsContainer') {
        const container = $(containerSelector);
        container.empty();

        if (!products || products.length === 0) {
            container.html('<div class="recommended-loading">No recommended items available right now.</div>');
            return;
        }

        const toShow = products.slice(0, 30);
        toShow.forEach(product => {
            const productId = product.productId || product.id;
            const $card = $('<div>').addClass('recommended-card').data('product', product);
            const imageUrl = normalizeImagePath(product.imagePath || product.imageUrl);
            const name = product.itemName || 'Untitled item';
            const price = product.price != null ? `₱${parseFloat(product.price).toFixed(2)}` : 'Price unavailable';

            $card.append(`<div class="recommended-image"><img src="${imageUrl}" alt="${name}"></div>`);
            $card.append(`<div class="recommended-overlay"><div class="recommended-actions"></div></div>`);
            if (!currentUserId) {
                $card.addClass('requires-login');
            }
            const $actions = $card.find('.recommended-actions');
            $actions.append($('<button>').addClass('recommended-price-pill').text(price).prop('disabled', true));
            $actions.append($('<button>').addClass('btn-add-cart-action').attr('type', 'button').text('ADD TO CART').click(function(event) {
                event.preventDefault();
                const userId = sessionStorage.getItem('currentUserId');
                if (!userId) {
                    const nextUrl = `./ItemDescription.html?productId=${encodeURIComponent(productId)}`;
                    sessionStorage.setItem('pendingRedirect', nextUrl);
                    const modal = document.getElementById('loginModalOverlay');
                    if (modal) {
                        modal.classList.add('active');
                        document.body.classList.add('modal-open');
                        return;
                    }
                    return window.location.href = './login.html?next=' + encodeURIComponent(nextUrl);
                }
                authFetch(`/api/cart/add?userId=${encodeURIComponent(userId)}&productId=${encodeURIComponent(productId)}`, { method: 'POST' })
                    .then(resp => {
                        if (!resp.ok) return resp.text().then(text => { throw new Error(text || 'Unable to add to cart'); });
                        alert(`Added ${name} to cart.`);
                    })
                    .catch(err => alert(err.message || 'Unable to add item to cart.'));
            }));
            $actions.append($('<button>').addClass('btn-view-item').attr('type', 'button').text('VIEW').click(function(event) {
                event.preventDefault();
                const userId = sessionStorage.getItem('currentUserId');
                if (!userId) {
                    const nextUrl = `./ItemDescription.html?productId=${encodeURIComponent(productId)}`;
                    sessionStorage.setItem('pendingRedirect', nextUrl);
                    const modal = document.getElementById('loginModalOverlay');
                    if (modal) {
                        modal.classList.add('active');
                        document.body.classList.add('modal-open');
                        return;
                    }
                    return window.location.href = './login.html?next=' + encodeURIComponent(nextUrl);
                }
                window.location.href = `./ItemDescription.html?productId=${productId}`;
            }));
            $card.append($actions);
            container.append($card);
        });
    }

    function loadRecommendedProductsInto(containerSelector, limit = 12) {
        const container = $(containerSelector);
        if (!container.length) return;

        container.html('<div class="recommended-loading">Loading recommendations…</div>');
        const query = currentUserId ? `?userId=${currentUserId}&limit=${limit}` : `?limit=${limit}`;

        $.get(`/api/products/recommended${query}`)
            .done(function(products) {
                renderRecommendedProducts(products, containerSelector);
            })
            .fail(function(xhr) {
                console.error('Failed to load recommendations:', xhr.responseText);
                container.html('<div class="recommended-loading">Unable to load recommendations.</div>');
            });
    }

    function loadYouMightAlsoLike(containerSelector, currentProductId, category, style, sellerId, limit = 6) {
        const container = $(containerSelector);
        if (!container.length) return;

        container.html('<div class="recommended-loading">Loading suggestions…</div>');
        const excludedProductId = Number(currentProductId);
        const desiredCategory = category ? String(category).trim().toLowerCase() : '';
        const desiredStyle = style ? String(style).trim().toLowerCase() : '';
        const desiredSellerId = sellerId != null ? String(sellerId) : null;

        const renderFallback = function() {
            const fallbackQuery = currentUserId ? `?userId=${currentUserId}` : '';
            $.get(`/api/products/recommended${fallbackQuery}`)
                .done(function(products) {
                    const filtered = (products || []).filter(product => String(product.productId) !== String(excludedProductId));
                    renderRecommendedProducts(filtered.slice(0, limit), containerSelector);
                })
                .fail(function(xhr) {
                    console.error('Failed to load suggestion products:', xhr.responseText);
                    container.html('<div class="recommended-loading">Unable to load suggestions.</div>');
                });
        };

        $.get('/api/products')
            .done(function(products) {
                const filtered = (products || []).filter(function(product) {
                    const productId = product.productId != null ? product.productId : product.id;
                    return String(productId) !== String(excludedProductId);
                });
                if (filtered.length === 0) {
                    renderFallback();
                    return;
                }

                const exactMatches = [];
                const categoryStyleMatches = [];
                const categoryMatches = [];
                const styleMatches = [];
                const sellerMatches = [];
                const others = [];

                filtered.forEach(function(product) {
                    const productSellerId = product.seller ? String(product.seller.userId || '') : '';
                    const productStyle = product.style ? String(product.style).trim().toLowerCase() : '';
                    const productCategory = product.category ? String(product.category).trim().toLowerCase() : '';

                    const sameSeller = desiredSellerId && productSellerId && productSellerId === desiredSellerId;
                    const sameStyle = desiredStyle && productStyle && productStyle === desiredStyle;
                    const sameCategory = desiredCategory && productCategory && productCategory === desiredCategory;

                    if (sameSeller && sameStyle && sameCategory) {
                        exactMatches.push(product);
                    } else if (sameCategory && sameStyle) {
                        categoryStyleMatches.push(product);
                    } else if (sameCategory) {
                        categoryMatches.push(product);
                    } else if (sameStyle) {
                        styleMatches.push(product);
                    } else if (sameSeller) {
                        sellerMatches.push(product);
                    } else {
                        others.push(product);
                    }
                });

                const sortedCandidates = [].concat(
                    exactMatches,
                    categoryStyleMatches,
                    categoryMatches,
                    styleMatches,
                    sellerMatches,
                    others
                );

                const matched = sortedCandidates.slice(0, limit);
                if (matched.length === 0) {
                    renderFallback();
                } else {
                    renderRecommendedProducts(matched, containerSelector);
                }
            })
            .fail(function(xhr) {
                console.error('Failed to load related products:', xhr.responseText);
                renderFallback();
            });
    }


    function renderCart() { console.log("Cart render baseline updated"); }


    function loadOrders(){
        if(!currentUserId){ $('#orders').text('Not logged in.'); return; }
        $('#orders').text('Loading orders…');
        $.get('/api/orders/customer/' + currentUserId)
            .done(function(data){ renderOrders(data); })
            .fail(function(xhr){ $('#orders').text('Failed to load orders: ' + xhr.responseText); });
    }


    function loadOrderItems(orderId, $container){
        $.get('/api/orders/' + orderId + '/items')
            .done(function(items){
                $container.empty();
                if(!items || items.length === 0){ $container.text('No order details found.'); return; }
                items.forEach(item => {
                    const productId = item.product?.productId || item.product?.id;
                    const $itemRow = $('<div>').addClass('order-item');
                    $itemRow.append($('<div>').html('<strong>' + (item.product?.itemName || 'Product') + '</strong> x' + (item.quantity || 1)));
                    $itemRow.append($('<div>').text('Price: ₱' + ((item.priceAtPurchase != null) ? item.priceAtPurchase : '0')));
                    if(currentUserRole === 'buyer'){
                        const $reviewBtn = $('<button>').text('Leave Review').click(function(){
                            showReviewForm(productId, item.product?.itemName || 'Product');
                        });
                        $itemRow.append($reviewBtn);
                    }
                    $container.append($itemRow);
                });
            })
            .fail(function(xhr){ $container.text('Unable to load order items: ' + xhr.responseText); });
    }


    function loadShipments(orderId, $container){
        $.get('/api/shipments/order/' + orderId)
            .done(function(shipments){
                $container.empty();
                if(!shipments || shipments.length === 0){ $container.text('No shipment information available yet.'); return; }
                shipments.forEach(ship => {
                    const status = ship.status || 'N/A';
                    const lines = [];
                    lines.push('Shipment status: ' + status);
                    if(ship.trackingNumber) lines.push('Tracking: ' + ship.trackingNumber);
                    if(ship.courierCompany) lines.push('Courier: ' + ship.courierCompany);
                    if(ship.estimatedDeliveryDate) lines.push('ETA: ' + ship.estimatedDeliveryDate);
                    if(ship.deliveredDate) lines.push('Delivered: ' + ship.deliveredDate);
                    if(ship.shippedDate) lines.push('Shipped: ' + ship.shippedDate);
                    $container.append($('<div>').html(lines.join('<br>')));
                });
            })
            .fail(function(xhr){ $container.text('Unable to load shipments: ' + xhr.responseText); });
    }


    function showReviewForm(productId, productName){
        const $form = $('<div>').addClass('review-form');
        $form.append($('<h4>').text('Review ' + productName));
        $form.append($('<label>').text('Rating: ').append($('<select>').attr('id', 'reviewRating')
            .append($('<option>').val('1').text('1'))
            .append($('<option>').val('2').text('2'))
            .append($('<option>').val('3').text('3'))
            .append($('<option>').val('4').text('4'))
            .append($('<option>').val('5').text('5'))));
        $form.append($('<br>'));
        $form.append($('<label>').text('Comment: ').append($('<textarea>').attr({id:'reviewComment',rows:3,cols:40})));  
        const $submit = $('<button>').text('Submit Review').click(function(){
            const payload = { buyerId: currentUserId, productId: productId, rating: parseInt($('#reviewRating').val(), 10), comment: $('#reviewComment').val() };
            $.ajax({ url: '/api/reviews', method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
                .done(function(){ alert('Review submitted.'); $form.remove(); })
                .fail(function(xhr){ alert('Review submission failed: ' + xhr.responseText); });
        });
        $form.append($('<div>').append($submit));
        $('#orders').prepend($form);
        $('html, body').animate({ scrollTop: $form.offset().top - 50 }, 400);
    }


    function loadProfile(){
        if(!currentUserId){ $('#profileInfo').text('Not logged in.'); $('#profileForm').hide(); return; }
        $('#profileInfo').text('Loading profile…');
        $.get('/api/profile/' + currentUserId)
            .done(function(user){
                const details = [];
                details.push('Name: ' + (user.fullName || 'N/A'));
                details.push('Email: ' + (user.email || 'N/A'));
                details.push('Contact: ' + (user.contactNo || 'N/A'));
                details.push('Role: ' + (user.role || 'N/A'));
                if(user.businessName) details.push('Business: ' + user.businessName);
                if(user.businessDescription) details.push('About: ' + user.businessDescription);
                $('#profileInfo').html(details.join('<br>'));
                $('#profileFullName').val(user.fullName || '');
                $('#profileContactNo').val(user.contactNo || '');
                $('#profileEmail').val(user.email || '');
                $('#profilePassword').val('');
                $('#profileForm').show();
                setSellerSection(user);
            })
            .fail(function(xhr){ $('#profileInfo').text('Failed to load profile: ' + xhr.responseText); $('#profileForm').hide(); });
    }


    $('#profileForm').on('submit', function(e){
        e.preventDefault();
        if(!currentUserId){ alert('Please login first'); return; }
        const payload = {
            fullName: $('#profileFullName').val(),
            contactNo: $('#profileContactNo').val(),
            email: $('#profileEmail').val()
        };
        const password = $('#profilePassword').val();
        if(password){ payload.password = password; }
        $.ajax({ url: '/api/profile/' + currentUserId, method: 'PUT', contentType: 'application/json', data: JSON.stringify(payload) })
            .done(function(){ $('#profileStatus').text('Profile updated.'); loadProfile(); })
            .fail(function(xhr){ $('#profileStatus').text('Update failed: ' + xhr.responseText); });
    });


    function loadNotifications(){
        if(!currentUserId){ $('#notifications').text('Not logged in.'); return; }
        $('#notifications').text('Loading notifications…');
        $.get('/api/notifications/user/' + currentUserId)
            .done(function(items){
                const $container = $('#notifications').empty();
                if(!items || items.length === 0){ $container.text('No notifications yet.'); return; }
                items.forEach(n => {
                    const $note = $('<div>').addClass('notification');
                    const status = n.isRead ? 'Read' : 'New';
                    $note.append($('<div>').html('<strong>' + n.title + '</strong> (' + status + ')'));
                    $note.append($('<div>').text(n.message || ''));
                    if(n.relatedOrderId){ $note.append($('<div>').text('Order: #' + n.relatedOrderId)); }
                    const $mark = $('<button>').text(n.isRead ? 'Mark read' : 'Mark as read').click(function(){
                        $.ajax({ url: '/api/notifications/' + n.notificationId + '/read', method: 'PUT' })
                            .done(function(){ loadNotifications(); })
                            .fail(function(xhr){ alert('Unable to mark notification as read: ' + xhr.responseText); });
                    });
                    $note.append($mark);
                    $container.append($note);
                });
            })
            .fail(function(xhr){ $('#notifications').text('Unable to load notifications: ' + xhr.responseText); });
    }


    // --- Register Form Actions ---
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


    // --- Checkout Actions ---
    $('#checkoutForm').on('submit', function(e){
        e.preventDefault();
        if(!currentUserId){ alert('Please login to checkout'); return; }
        const payload = { shippingAddress: $('#shippingAddress').val(), paymentMethod: $('#paymentMethod').val() };
        $.ajax({ url: '/api/orders/checkout/' + currentUserId, method: 'POST', contentType: 'application/json', data: JSON.stringify(payload) })
            .done(function(){ alert('Order placed'); $('#checkoutForm')[0].reset(); loadOrders(); loadCart(); })
            .fail(function(xhr){ alert('Checkout failed: ' + xhr.responseText); });
    });


    // --- Seller Product Form Actions ---
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
    }


    // --- Seller Requests Pipelines ---
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


    function renderAdminRequests(list){
        const $c = $('#adminRequests').empty();
        if(!list || list.length === 0){ $c.text('No requests found.'); return; }
        list.forEach(r => {
            const $card = $('<div>').addClass('admin-request');
            $card.append($('<div>').html('<strong>Request #' + (r.requestId || r.id || 'N/A') + '</strong> — Status: ' + (r.status || 'PENDING')));
            $card.append($('<div>').text('Business: ' + (r.businessName || '')));
            $card.append($('<div>').text('Seller: ' + ((r.user && (r.user.fullName || r.user.email)) || 'Unknown')));
            $card.append($('<div>').text('Description: ' + (r.businessDescription || '')));
            const $select = $('<select>').append($('<option>').val('PENDING').text('PENDING')).append($('<option>').val('APPROVED').text('APPROVE')).append($('<option>').val('REJECTED').text('REJECT'));
            $select.val(r.status || 'PENDING');
            const $btn = $('<button>').text('Apply').click(function(){
                const newStatus = $select.val();
                const adminId = currentUserId || 1;
                $.ajax({ url: '/api/admin/requests/' + (r.requestId || r.id) + '/decide?status=' + newStatus + '&adminId=' + adminId, method: 'PUT' })
                    .done(function(){ loadAdminRequests(); $('#verifySellerStatus').text('Request updated.'); loadSellerRequests(); })
                    .fail(function(xhr){ $('#verifySellerStatus').text('Update failed: ' + xhr.responseText); });
            });
            $card.append($('<div>').append('Decision: ', $select, ' ', $btn));
            $c.append($card);
        });
    }


    function loadAdminRequests(){
        $('#adminRequests').text('Loading requests…');
        $.get('/api/admin/requests/')
            .done(renderAdminRequests)
            .fail(function(xhr){ $('#adminRequests').text('Failed to load requests: ' + xhr.responseText); });
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
            });
    }


    $('#loadSellerDetailsBtn').on('click', loadSellerDetails);




    // ==========================================================================
// MODAL LOGIN HANDLERS
// ==========================================================================
const loginModal = document.getElementById('loginModalOverlay');
const closeModalBtn = document.getElementById('closeModalBtn');
const modalLoginForm = document.getElementById('modalLoginForm');


if (loginModal) {
    loginModal.addEventListener('click', (e) => {
        if (e.target === loginModal) {
            loginModal.classList.remove('active');
            document.body.classList.remove('modal-open');
        }
    });
}

if (closeModalBtn && loginModal) {
    closeModalBtn.addEventListener('click', () => {
        loginModal.classList.remove('active');
        document.body.classList.remove('modal-open');
    });
   
    // Close if they click the dark background outside the card
    loginModal.addEventListener('click', (e) => {
        if (e.target === loginModal) {
            loginModal.classList.remove('active');
            document.body.classList.remove('modal-open');
        }
    });
}


if (modalLoginForm) {
    modalLoginForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        const emailValue = document.getElementById('modalEmail').value.trim();
        const passwordValue = document.getElementById('modalPassword').value;


        if (!emailValue || !passwordValue) {
            alert('Email and password are required.');
            return;
        }


        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: emailValue, password: passwordValue })
            });


            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Login failed');
            }


            const data = await response.json();
            sessionStorage.setItem('authToken', data.token);
            sessionStorage.setItem('currentUserId', data.userId);
            sessionStorage.setItem('currentUserRole', data.role);
            sessionStorage.setItem('currentUserEmail', data.email);
            sessionStorage.setItem('currentUserFullName', data.fullName);


            const redirectUrl = sessionStorage.getItem('pendingRedirect') || window.location.href;
            sessionStorage.removeItem('pendingRedirect');
            closeLoginModal();
            window.location.href = redirectUrl;
        } catch (error) {
            alert('Login failed: ' + (error.message || 'Invalid credentials'));
        }
    });
}


function closeLoginModal() {
    if (!loginModal) return;
    loginModal.classList.remove('active');
    document.body.classList.remove('modal-open');
}


// Remove modal open state if login modal is dismissed elsewhere
if (loginModal) {
    loginModal.addEventListener('transitionend', () => {
        if (!loginModal.classList.contains('active')) {
            document.body.classList.remove('modal-open');
        }
    });
}
});



