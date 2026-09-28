/**
 * WMS Dashboard - Client-side Interactive Logic
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Transaction Filter Pills
    const filterPills = document.querySelectorAll('.pill-btn');
    const tableRows = document.querySelectorAll('.wms-table tbody tr');

    filterPills.forEach(pill => {
        pill.addEventListener('click', () => {
            filterPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');

            const filterValue = pill.getAttribute('data-filter');

            tableRows.forEach(row => {
                const rowType = row.getAttribute('data-type');
                if (filterValue === 'all' || rowType === filterValue) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    });

    // 2. Real-time Search input
    const searchInput = document.getElementById('tableSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            tableRows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(query)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // 3. Modal controls
    const modal = document.getElementById('ticketModal');
    const modalCloseBtn = document.getElementById('modalCloseBtn');
    const modalCancelBtn = document.getElementById('modalCancelBtn');
    const modalSubmitBtn = document.getElementById('modalSubmitBtn');
    const modalTitle = document.getElementById('modalTitle');

    function openModal(title, type) {
        if (!modal) return;
        modalTitle.textContent = title;
        document.getElementById('modalTypeSelect').value = type;
        modal.classList.add('show');
    }

    function closeModal() {
        if (!modal) return;
        modal.classList.remove('show');
    }

    if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
    if (modalCancelBtn) modalCancelBtn.addEventListener('click', closeModal);

    // Close on clicking overlay outside modal content
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal();
        });
    }

    // Quick action buttons
    const btnInbound = document.getElementById('btnQuickInbound');
    const btnOutbound = document.getElementById('btnQuickOutbound');
    const btnAddProduct = document.getElementById('btnQuickAddProduct');
    const btnAudit = document.getElementById('btnQuickAudit');
    const btnExport = document.getElementById('btnQuickExport');

    if (btnInbound) {
        btnInbound.addEventListener('click', () => {
            openModal('Tạo phiếu Nhập kho mới', 'IN');
        });
    }

    if (btnOutbound) {
        btnOutbound.addEventListener('click', () => {
            openModal('Tạo phiếu Xuất kho mới', 'OUT');
        });
    }

    if (btnAddProduct) {
        btnAddProduct.addEventListener('click', () => {
            openModal('Thêm sản phẩm mới vào kho', 'PRODUCT');
        });
    }

    if (btnAudit) {
        btnAudit.addEventListener('click', () => {
            showToast('✅ Đã khởi tạo phiên kiểm kê định kỳ kho bãi!');
        });
    }

    if (btnExport) {
        btnExport.addEventListener('click', () => {
            showToast('📊 Đang xuất báo cáo tổng hợp kho hàng (.xlsx)...');
            setTimeout(() => {
                showToast('✅ Đã xuất báo cáo kho hàng thành công!');
            }, 1200);
        });
    }

    if (modalSubmitBtn) {
        modalSubmitBtn.addEventListener('click', () => {
            const code = document.getElementById('modalCodeInput').value || 'NK-' + Math.floor(1000 + Math.random() * 9000);
            closeModal();
            showToast(`✅ Phiếu ${code} đã được lưu thành công vào hệ thống!`);
        });
    }

    // View detail buttons
    document.querySelectorAll('.table-action-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const code = row.querySelector('.ticket-code').textContent;
            showToast(`🔍 Đang mở chi tiết chứng từ kho: ${code}`);
        });
    });

    // Stock alert order buttons
    document.querySelectorAll('.btn-restock').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const sku = e.target.getAttribute('data-sku');
            openModal(`Tạo yêu cầu nhập bổ sung SKU: ${sku}`, 'IN');
        });
    });
});

// Toast notification helper
function showToast(message) {
    let toast = document.getElementById('toastNotice');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toastNotice';
        toast.className = 'toast-notice';
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}
