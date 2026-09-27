(() => {
    const sidebar = document.querySelector('#sidebar');
    const toggle = document.querySelector('#menu-toggle');
    const overlay = document.querySelector('#sidebar-overlay');
    const closeMenu = () => {
        sidebar?.classList.remove('open');
        overlay?.classList.remove('show');
        toggle?.setAttribute('aria-expanded', 'false');
    };
    toggle?.addEventListener('click', () => {
        const open = sidebar?.classList.toggle('open');
        overlay?.classList.toggle('show', open);
        toggle.setAttribute('aria-expanded', String(Boolean(open)));
    });
    overlay?.addEventListener('click', closeMenu);
    document.addEventListener('keydown', event => { if (event.key === 'Escape') closeMenu(); });

    document.querySelectorAll('[data-dismiss-toast]').forEach(button => {
        button.addEventListener('click', () => button.closest('.toast')?.remove());
    });
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            if (!window.confirm(form.dataset.confirm || 'Bạn có chắc chắn muốn tiếp tục?')) event.preventDefault();
        });
    });

    const passwordToggle = document.querySelector('[data-password-toggle]');
    passwordToggle?.addEventListener('click', () => {
        const input = document.querySelector('#password');
        if (!input) return;
        input.type = input.type === 'password' ? 'text' : 'password';
        passwordToggle.textContent = input.type === 'password' ? 'Hiện' : 'Ẩn';
        passwordToggle.setAttribute('aria-label', input.type === 'password' ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
    });

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
})();
