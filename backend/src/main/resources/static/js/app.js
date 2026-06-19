document.addEventListener("DOMContentLoaded", () => {
    
    // ==========================================================================
    // 1. BUY/SELL NAVBAR TOGGLE STATES
    // ==========================================================================
    const toggleButtons = document.querySelectorAll(".toggle-btn");
    toggleButtons.forEach(button => {
        button.addEventListener("click", () => {
            toggleButtons.forEach(btn => btn.classList.remove("active"));
            button.classList.add("active");
        });
    });

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
        
        // Deactivate all active slides smoothly via CSS transitions
        slides.forEach(slide => slide.classList.remove("active"));
        
        currentIdx = index;
        // Keep index looping mathematically bound within array length
        if (currentIdx >= slides.length) currentIdx = 0;
        if (currentIdx < 0) currentIdx = slides.length - 1;
        
        // Wake up targeted slide view layer
        slides[currentIdx].classList.add("active");
    }

    function nextSlide() {
        showSlide(currentIdx + 1);
    }

    function startAutoplay() {
        if (slides.length > 0) {
            slideInterval = setInterval(nextSlide, 5000); // Changes background image automatically every 5 seconds
        }
    }

    function resetAutoplay() {
        clearInterval(slideInterval);
        startAutoplay();
    }

    // Set interactive event tracking for manual slide stepping controls
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

    // Fire initialization threads
    startAutoplay();

    // ==========================================================================
    // 3. STEP 3: ROUTING SECURITY GATEWAY INTERCEPTORS
    // ==========================================================================
    const protectedElements = document.querySelectorAll(
        '.profile-trigger, .cart-trigger, .shop-trigger, .category-card, .masonry-item'
    );

    protectedElements.forEach(element => {
        element.addEventListener("click", (event) => {
            event.preventDefault(); // Stop raw standard anchor links or event execution bubbles
            console.log("🔒 Route Lock Intercepted: Redirecting user session to authentication card...");
            window.location.href = "/login.html"; // Redirects smoothly right into your compact login system card view
        });
    });

});