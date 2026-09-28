(() => {
    'use strict';

    const root = document.documentElement;
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    const updateThemeControls = () => {
        const dark = root.dataset.theme === 'dark';
        document.querySelectorAll('[data-theme-icon]').forEach(icon => {
            icon.classList.toggle('bi-moon-stars', !dark);
            icon.classList.toggle('bi-sun', dark);
        });
        document.querySelectorAll('#theme-toggle').forEach(button => {
            const label = dark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối';
            button.setAttribute('aria-label', label);
            button.setAttribute('title', label);
        });
        document.querySelector('meta[name="theme-color"]')?.setAttribute('content', dark ? '#071313' : '#102b2b');
    };

    document.querySelectorAll('#theme-toggle').forEach(button => {
        button.addEventListener('click', () => {
            const nextTheme = root.dataset.theme === 'dark' ? 'light' : 'dark';
            root.dataset.theme = nextTheme;
            try { localStorage.setItem('docucatalog-theme', nextTheme); } catch (_) { /* Storage may be disabled. */ }
            updateThemeControls();
            window.dispatchEvent(new CustomEvent('docucatalog:theme'));
        });
    });
    updateThemeControls();

    const sidebar = document.querySelector('#sidebar');
    const menuToggle = document.querySelector('#menu-toggle');
    const sidebarOverlay = document.querySelector('#sidebar-overlay');
    const closeMenu = () => {
        sidebar?.classList.remove('open');
        sidebarOverlay?.classList.remove('show');
        menuToggle?.setAttribute('aria-expanded', 'false');
    };
    menuToggle?.addEventListener('click', () => {
        const open = sidebar?.classList.toggle('open');
        sidebarOverlay?.classList.toggle('show', open);
        menuToggle.setAttribute('aria-expanded', String(Boolean(open)));
    });
    sidebarOverlay?.addEventListener('click', closeMenu);
    document.addEventListener('keydown', event => { if (event.key === 'Escape') closeMenu(); });

    const dismissToast = toast => {
        toast?.classList.add('toast-hiding');
        window.setTimeout(() => toast?.remove(), reducedMotion.matches ? 0 : 190);
    };
    document.querySelectorAll('.toast').forEach(toast => {
        const timer = window.setTimeout(() => dismissToast(toast), 5200);
        toast.addEventListener('mouseenter', () => window.clearTimeout(timer), { once: true });
    });
    document.querySelectorAll('[data-dismiss-toast]').forEach(button => {
        button.addEventListener('click', () => dismissToast(button.closest('.toast')));
    });

    const confirmDialog = document.querySelector('#confirm-dialog');
    const confirmMessage = confirmDialog?.querySelector('#confirm-dialog-message');
    const confirmAccept = confirmDialog?.querySelector('[data-confirm-accept]');
    const confirmCancel = confirmDialog?.querySelector('[data-confirm-cancel]');
    let pendingForm = null;
    let pendingSubmitter = null;

    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            if (form.dataset.confirmed === 'true') {
                delete form.dataset.confirmed;
                return;
            }
            event.preventDefault();
            if (!confirmDialog?.showModal) {
                if (window.confirm(form.dataset.confirm || 'Bạn có chắc chắn muốn tiếp tục?')) {
                    form.dataset.confirmed = 'true';
                    form.requestSubmit(event.submitter);
                }
                return;
            }
            pendingForm = form;
            pendingSubmitter = event.submitter;
            if (confirmMessage) confirmMessage.textContent = form.dataset.confirm || 'Bạn có chắc chắn muốn tiếp tục?';
            confirmDialog.showModal();
            window.setTimeout(() => confirmCancel?.focus(), 0);
        });
    });
    confirmCancel?.addEventListener('click', () => confirmDialog.close());
    confirmAccept?.addEventListener('click', () => {
        if (!pendingForm) return;
        const form = pendingForm;
        const submitter = pendingSubmitter;
        pendingForm = null;
        pendingSubmitter = null;
        confirmDialog.close();
        form.dataset.confirmed = 'true';
        form.requestSubmit(submitter || undefined);
    });
    confirmDialog?.addEventListener('close', () => {
        pendingForm = null;
        pendingSubmitter = null;
    });

    document.querySelectorAll('[data-password-toggle], [data-password-toggle-for]').forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.dataset.passwordToggleFor || 'password';
            const input = document.getElementById(targetId);
            if (!input) return;
            const show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            const icon = button.querySelector('i');
            if (icon) {
                icon.classList.toggle('bi-eye', !show);
                icon.classList.toggle('bi-eye-slash', show);
            } else {
                button.textContent = show ? 'Ẩn' : 'Hiện';
            }
            button.setAttribute('aria-label', `${show ? 'Ẩn' : 'Hiện'} mật khẩu`);
        });
    });

    document.querySelectorAll('[data-password-strength]').forEach(meter => {
        const input = document.querySelector('#newPassword');
        const label = meter.querySelector('.password-strength-label');
        const update = () => {
            const value = input?.value || '';
            let score = 0;
            if (value.length >= 8) score++;
            if (value.length >= 12) score++;
            if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score++;
            if (/\d/.test(value) && /[^A-Za-z0-9]/.test(value)) score++;
            meter.dataset.score = String(Math.min(score, 4));
            if (!label) return;
            label.textContent = value.length === 0 ? 'Nhập ít nhất 8 ký tự'
                : score <= 1 ? 'Mức bảo mật: yếu'
                : score === 2 ? 'Mức bảo mật: trung bình'
                : score === 3 ? 'Mức bảo mật: tốt'
                : 'Mức bảo mật: mạnh';
        };
        input?.addEventListener('input', update);
        update();
    });

    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', event => {
            if (event.defaultPrevented || !form.checkValidity()) return;
            const submitter = event.submitter || form.querySelector('button[type="submit"]');
            if (!submitter) return;
            submitter.classList.add('is-loading');
            submitter.setAttribute('aria-busy', 'true');
            const label = submitter.dataset.loadingLabel;
            if (label) submitter.textContent = label;
            window.setTimeout(() => { submitter.disabled = true; }, 0);
        });
    });

    document.querySelectorAll('[data-guard-unsaved]').forEach(form => {
        let dirty = false;
        let submitted = false;
        form.addEventListener('input', () => { dirty = true; });
        form.addEventListener('change', () => { dirty = true; });
        form.addEventListener('submit', () => { submitted = true; });
        window.addEventListener('beforeunload', event => {
            if (!dirty || submitted) return;
            event.preventDefault();
            event.returnValue = '';
        });
    });

    document.querySelectorAll('[data-character-count]').forEach(counter => {
        const input = document.getElementById(counter.dataset.characterCount);
        const update = () => { counter.textContent = String(input?.value.length || 0); };
        input?.addEventListener('input', update);
        update();
    });

    document.querySelectorAll('[data-avatar-input]').forEach(input => {
        const preview = document.querySelector('[data-avatar-preview]');
        const fileName = document.querySelector('[data-avatar-file-name]');
        let objectUrl = null;
        input.addEventListener('change', () => {
            const file = input.files?.[0];
            if (!file) return;
            if (fileName) fileName.textContent = `${file.name} · ${(file.size / 1024 / 1024).toFixed(2)} MB`;
            if (!preview || !file.type.startsWith('image/')) return;
            if (objectUrl) URL.revokeObjectURL(objectUrl);
            objectUrl = URL.createObjectURL(file);
            preview.replaceChildren();
            const image = document.createElement('img');
            image.src = objectUrl;
            image.alt = 'Xem trước ảnh đại diện mới';
            preview.appendChild(image);
        });
        window.addEventListener('beforeunload', () => { if (objectUrl) URL.revokeObjectURL(objectUrl); });
    });

    const firstInvalid = document.querySelector('.input-invalid, [aria-invalid="true"]');
    if (firstInvalid) window.setTimeout(() => firstInvalid.focus(), 80);

    const items = document.querySelector('#acquisition-items');
    const template = document.querySelector('#acquisition-item-template');
    const addButton = document.querySelector('#add-acquisition-item');
    let itemIndex = items?.querySelectorAll('.acquisition-row').length || 0;
    const formatMoney = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);
    const recalculate = () => {
        let grandTotal = 0;
        items?.querySelectorAll('.acquisition-row').forEach(row => {
            const quantity = Number(row.querySelector('[data-quantity]')?.value || 0);
            const price = Number(row.querySelector('[data-price]')?.value || 0);
            const total = quantity * price;
            grandTotal += total;
            const target = row.querySelector('[data-row-total]');
            if (target) target.textContent = formatMoney(total);
        });
        const grand = document.querySelector('#grand-total');
        if (grand) grand.textContent = formatMoney(grandTotal);
    };
    const bindRow = row => {
        row.querySelectorAll('[data-quantity], [data-price]').forEach(input => input.addEventListener('input', recalculate));
        row.querySelector('[data-remove-item]')?.addEventListener('click', () => {
            if (items.querySelectorAll('.acquisition-row').length <= 1) return;
            row.remove();
            recalculate();
        });
    };
    items?.querySelectorAll('.acquisition-row').forEach(bindRow);
    addButton?.addEventListener('click', () => {
        if (!template || !items) return;
        const html = template.innerHTML.replaceAll('__INDEX__', String(itemIndex++));
        const wrapper = document.createElement('div');
        wrapper.innerHTML = html.trim();
        const row = wrapper.firstElementChild;
        items.appendChild(row);
        bindRow(row);
        row.querySelector('select')?.focus();
        recalculate();
    });
    recalculate();

    const canvas = document.querySelector('[data-particles]');
    if (canvas) {
        const context = canvas.getContext('2d', { alpha: true });
        let particles = [];
        let animationFrame = null;
        let running = true;
        const lowPower = (navigator.hardwareConcurrency || 4) <= 2;

        const particleColor = alpha => {
            const rgb = getComputedStyle(root).getPropertyValue('--particle-rgb').trim() || '31, 112, 103';
            return `rgba(${rgb}, ${alpha})`;
        };
        const resize = () => {
            const ratio = Math.min(window.devicePixelRatio || 1, 1.5);
            canvas.width = Math.floor(window.innerWidth * ratio);
            canvas.height = Math.floor(window.innerHeight * ratio);
            canvas.style.width = `${window.innerWidth}px`;
            canvas.style.height = `${window.innerHeight}px`;
            context.setTransform(ratio, 0, 0, ratio, 0, 0);
            const count = reducedMotion.matches ? 16 : Math.min(lowPower ? 18 : 30, Math.max(18, Math.floor(window.innerWidth / 55)));
            particles = Array.from({ length: count }, () => ({
                x: Math.random() * window.innerWidth,
                y: Math.random() * window.innerHeight,
                radius: Math.random() * 1.5 + .6,
                dx: (Math.random() - .5) * .12,
                dy: (Math.random() - .5) * .12
            }));
        };
        const draw = () => {
            context.clearRect(0, 0, window.innerWidth, window.innerHeight);
            context.fillStyle = particleColor(.25);
            particles.forEach((particle, index) => {
                context.beginPath();
                context.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
                context.fill();
                for (let second = index + 1; second < particles.length; second++) {
                    const other = particles[second];
                    const dx = particle.x - other.x;
                    const dy = particle.y - other.y;
                    const distance = Math.hypot(dx, dy);
                    if (distance < 118) {
                        context.strokeStyle = particleColor((1 - distance / 118) * .075);
                        context.lineWidth = .7;
                        context.beginPath();
                        context.moveTo(particle.x, particle.y);
                        context.lineTo(other.x, other.y);
                        context.stroke();
                    }
                }
            });
        };
        const animate = () => {
            if (!running) return;
            particles.forEach(particle => {
                particle.x += particle.dx;
                particle.y += particle.dy;
                if (particle.x < -10) particle.x = window.innerWidth + 10;
                if (particle.x > window.innerWidth + 10) particle.x = -10;
                if (particle.y < -10) particle.y = window.innerHeight + 10;
                if (particle.y > window.innerHeight + 10) particle.y = -10;
            });
            draw();
            animationFrame = window.requestAnimationFrame(animate);
        };
        const restart = () => {
            if (animationFrame) window.cancelAnimationFrame(animationFrame);
            draw();
            if (!reducedMotion.matches && !document.hidden) animate();
        };
        resize();
        restart();
        window.addEventListener('resize', () => { resize(); restart(); }, { passive: true });
        window.addEventListener('docucatalog:theme', draw);
        reducedMotion.addEventListener?.('change', restart);
        document.addEventListener('visibilitychange', () => {
            running = !document.hidden;
            if (running) restart();
            else if (animationFrame) window.cancelAnimationFrame(animationFrame);
        });
    }
})();
