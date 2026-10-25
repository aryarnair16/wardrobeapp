// WearNet - Main Web Application State & Logic

let currentUserId = 1; // Arya by default
let currentUser = null;
let allUsers = [];
let myWardrobe = [];
let peerWardrobe = [];
let currentCategoryFilter = 'ALL';
let currentOutfit = null;
let lockedSlots = {
  Top: null,
  Bottom: null,
  Outerwear: null,
  Footwear: null,
  Accessory: null
};

let activeHandoverReqId = null;
let activeReturnReqId = null;
let activeTargetItem = null;

// INIT
document.addEventListener('DOMContentLoaded', async () => {
  await loadUsers();
  await refreshActiveUserData();
  switchTab('dashboard');
});

// USER SWITCHER & AUTH
async function loadUsers() {
  try {
    const res = await fetch('/api/auth/users');
    const data = await res.json();
    if (data.success) {
      allUsers = data.users;
      renderPilotSwitcherList();
    }
  } catch (e) {
    console.error("Error loading users", e);
  }
}

async function refreshActiveUserData() {
  try {
    const res = await fetch(`/api/auth/me?userId=${currentUserId}`);
    const data = await res.json();
    if (data.success && data.user) {
      currentUser = data.user;
      updateUserNavUI();
      await loadDashboardData();
    }
  } catch (e) {
    console.error("Error refreshing active user", e);
  }
}

function updateUserNavUI() {
  if (!currentUser) return;
  document.getElementById('navUsername').textContent = currentUser.username;
  document.getElementById('navScore').textContent = `★ ${currentUser.trustScore.toFixed(1)} (${currentUser.totalRatings} Reviews)`;
  if (currentUser.avatar) {
    document.getElementById('navAvatar').src = currentUser.avatar;
  }
  document.getElementById('dashWelcomeName').textContent = currentUser.username;
  document.getElementById('trustBigScore').textContent = currentUser.trustScore.toFixed(1);
  document.getElementById('trustTotalReviews').textContent = `Based on ${currentUser.totalRatings} verified peer exchanges`;
}

function openUserSwitcherModal() {
  renderPilotSwitcherList();
  openModal('modalUserSwitcher');
}

function renderPilotSwitcherList() {
  const container = document.getElementById('pilotUserList');
  if (!container) return;
  container.innerHTML = allUsers.map(u => `
    <div style="display:flex; align-items:center; justify-content:space-between; padding:0.85rem; border-radius:12px; border:1.5px solid ${u.userId === currentUserId ? '#6D2E3A' : 'var(--border)'}; background:${u.userId === currentUserId ? '#FDEDE7' : '#fff'}; cursor:pointer;" onclick="switchUser(${u.userId})">
      <div style="display:flex; align-items:center; gap:0.75rem;">
        <img src="${u.avatar || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'}" style="width:40px; height:40px; border-radius:50%; object-fit:cover; border:1.5px solid ${u.userId === currentUserId ? '#6D2E3A' : '#E09A8A'};">
        <div>
          <div style="font-weight:800; font-size:0.9rem; color:#4B1D27;">${u.username} ${u.userId === currentUserId ? '<span style="font-size:0.75rem; color:#6D2E3A; font-weight:700;">(Active)</span>' : ''}</div>
          <div style="font-size:0.75rem; color:var(--text-muted); font-weight:600;">${u.neighborhood || 'Central Campus'} • <span style="color:#C86A5A; font-weight:700;">★ ${u.trustScore.toFixed(1)}</span></div>
        </div>
      </div>
      <button class="btn btn-sm ${u.userId === currentUserId ? 'btn-primary' : 'btn-outline'}">
        ${u.userId === currentUserId ? 'Selected' : 'Switch'}
      </button>
    </div>
  `).join('');
}

async function switchUser(userId) {
  currentUserId = userId;
  closeModal('modalUserSwitcher');
  showToast(`Switched active profile to ${allUsers.find(u => u.userId === userId)?.username || 'User'}`);
  await refreshActiveUserData();
  unlockAllSlots();
  // Refresh whichever tab is active
  const activeTab = document.querySelector('.nav-link.active')?.getAttribute('onclick')?.match(/'([^']+)'/)?.[1] || 'dashboard';
  switchTab(activeTab);
}

// TAB NAVIGATION
function switchTab(tabId) {
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.remove('active');
    if (link.getAttribute('onclick')?.includes(`'${tabId}'`)) {
      link.classList.add('active');
    }
  });

  document.querySelectorAll('.tab-pane').forEach(pane => {
    pane.classList.remove('active');
  });

  const targetPane = document.getElementById(`tab-${tabId}`);
  if (targetPane) targetPane.classList.add('active');

  // Trigger relevant data loader
  if (tabId === 'dashboard') loadDashboardData();
  else if (tabId === 'wardrobe') loadWardrobeData();
  else if (tabId === 'generator') runGenerator();
  else if (tabId === 'lending') loadLendingHub();
  else if (tabId === 'verification') loadVerificationDemoPreset('pristine');
  else if (tabId === 'complaints') loadComplaints();
  else if (tabId === 'thrift') loadThriftStore();
  else if (tabId === 'ratings') loadRatingsWall();
}

// DASHBOARD
async function loadDashboardData() {
  try {
    const [wardrobeRes, requestsRes, peersRes, complaintsRes] = await Promise.all([
      fetch(`/api/wardrobe?userId=${currentUserId}`),
      fetch(`/api/lending/requests?userId=${currentUserId}`),
      fetch(`/api/auth/users`),
      fetch(`/api/complaints?userId=${currentUserId}`)
    ]);

    const wardrobeData = await wardrobeRes.json();
    const requestsData = await requestsRes.json();
    const peersData = await peersRes.json();
    const complaintsData = await complaintsRes.json();

    const items = wardrobeData.items || [];
    document.getElementById('statTotalItems').textContent = items.length;
    document.getElementById('statLentOut').textContent = items.filter(i => i.isLent).length;
    document.getElementById('statTrustScore').textContent = currentUser ? currentUser.trustScore.toFixed(1) : '5.0';
    document.getElementById('statComplaints').textContent = complaintsData.complaints ? complaintsData.complaints.filter(c => c.status === 'OPEN').length : '0';

    // Active Queue Render
    const queueWrap = document.getElementById('dashLendingQueue');
    const allReqs = [...(requestsData.incoming || []), ...(requestsData.outgoing || [])];
    if (allReqs.length === 0) {
      queueWrap.innerHTML = `<p style="color:var(--text-muted); font-size:0.88rem;">No active lending or borrowing requests right now.</p>`;
    } else {
      queueWrap.innerHTML = allReqs.slice(0, 3).map(r => `
        <div style="display:flex; align-items:center; justify-content:space-between; padding:0.75rem; border-radius:8px; background:#FFF9F7; border:1px solid var(--border); margin-bottom:0.5rem;">
          <div style="display:flex; align-items:center; gap:0.75rem;">
            <img src="${r.itemImagePath || 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=150'}" style="width:48px; height:48px; border-radius:8px; object-fit:cover;">
            <div>
              <div style="font-weight:700; font-size:0.88rem;">${r.itemName}</div>
              <div style="font-size:0.75rem; color:var(--text-muted);">${r.ownerId === currentUserId ? `Lent to ${r.requesterName}` : `Borrowed from ${r.ownerName}`} • ${r.status}</div>
            </div>
          </div>
          <span class="status-badge status-${r.status}">${r.status}</span>
        </div>
      `).join('');
    }

    // Pilot Peers Render
    const peerGrid = document.getElementById('dashPeerGrid');
    const otherUsers = (peersData.users || []).filter(u => u.userId !== currentUserId);
    peerGrid.innerHTML = otherUsers.map(u => `
      <div style="background:#FFF9F7; border:1px solid var(--border); border-radius:10px; padding:0.85rem; display:flex; align-items:center; gap:0.75rem;">
        <img src="${u.avatar}" style="width:45px; height:45px; border-radius:50%; object-fit:cover;">
        <div>
          <div style="font-weight:700; font-size:0.85rem;">${u.username}</div>
          <div style="font-size:0.72rem; color:var(--text-muted);">${u.neighborhood}</div>
          <div style="font-size:0.72rem; color:#C86A5A; font-weight:600;">★ ${u.trustScore.toFixed(1)}</div>
        </div>
      </div>
    `).join('');

  } catch (e) {
    console.error("Dashboard error", e);
  }
}

// DIGITAL WARDROBE
async function loadWardrobeData() {
  try {
    const res = await fetch(`/api/wardrobe?userId=${currentUserId}`);
    const data = await res.json();
    if (data.success) {
      myWardrobe = data.items;
      renderWardrobeGrid();
    }
  } catch (e) {
    console.error("Wardrobe fetch error", e);
  }
}

function filterWardrobe(category) {
  currentCategoryFilter = category;
  document.querySelectorAll('#categoryFilters .filter-btn').forEach(btn => {
    btn.classList.remove('active');
    if (btn.textContent.includes(category) || (category === 'ALL' && btn.textContent.includes('All'))) {
      btn.classList.add('active');
    }
  });
  renderWardrobeGrid();
}

function renderWardrobeGrid() {
  const container = document.getElementById('wardrobeGrid');
  if (!container) return;

  let filtered = myWardrobe;
  if (currentCategoryFilter === 'THRIFT') {
    filtered = myWardrobe.filter(i => i.isForSale);
  } else if (currentCategoryFilter !== 'ALL') {
    filtered = myWardrobe.filter(i => i.category.toLowerCase() === currentCategoryFilter.toLowerCase());
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 3rem 1rem; color:var(--text-muted);">
        <div style="font-size:2.5rem; margin-bottom:0.5rem;">🧥</div>
        <p style="font-weight:600;">No pieces found in this category.</p>
        <button class="btn btn-primary btn-sm" onclick="openAddItemModal()" style="margin-top:0.75rem;">Upload First Piece</button>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(item => `
    <div class="item-card">
      <div class="item-img-wrap">
        <img src="${item.imagePath}" alt="${item.name}" class="item-img" onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500'">
        <span class="badge ${item.isLent ? 'badge-lent' : 'badge-avail'}">
          ${item.isLent ? 'Lent Out' : 'In Wardrobe'}
        </span>
        ${item.isForSale ? `<span class="badge-thrift">Thrift: ₹${item.price}</span>` : ''}
      </div>
      <div class="item-details">
        <h4 class="item-name">${item.name}</h4>
        <div class="item-meta">
          <span class="tag-pill">${item.category}</span>
          <span class="tag-pill">${item.size}</span>
          <span class="tag-pill">${item.style}</span>
          <span class="tag-pill">₹${item.originalValue} Val</span>
        </div>
        <div class="item-actions">
          <button class="btn btn-outline btn-sm" style="flex:1;" onclick="toggleThriftSale(${item.itemId}, ${!item.isForSale})">
            ${item.isForSale ? 'Remove Thrift' : '🏷️ List Thrift'}
          </button>
          <button class="btn btn-secondary btn-sm" onclick="deleteItem(${item.itemId})" title="Delete piece">🗑️</button>
        </div>
      </div>
    </div>
  `).join('');
}

async function toggleThriftSale(itemId, enable) {
  let price = 0;
  if (enable) {
    const input = prompt("Enter thrift asking price in INR (₹):", "799");
    if (!input || isNaN(input)) return;
    price = parseFloat(input);
  }
  try {
    const res = await fetch('/api/wardrobe/thrift-toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemId, userId: currentUserId, isForSale: enable, price })
    });
    const data = await res.json();
    if (data.success) {
      showToast(enable ? `Listed in Thrift Store for ₹${price}!` : "Removed from Thrift Store.");
      loadWardrobeData();
    }
  } catch (e) {
    console.error("Error toggling thrift", e);
  }
}

async function deleteItem(itemId) {
  if (!confirm("Are you sure you want to remove this piece from your wardrobe?")) return;
  try {
    const res = await fetch(`/api/wardrobe?itemId=${itemId}&userId=${currentUserId}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast("Item deleted successfully.");
      loadWardrobeData();
    }
  } catch (e) {
    console.error("Delete error", e);
  }
}

// AI OUTFIT GENERATOR
async function runGenerator() {
  const occasion = document.getElementById('genOccasion')?.value || 'Casual';
  const season = document.getElementById('genSeason')?.value || 'All-Season';

  let query = `/api/outfits/generate?userId=${currentUserId}&occasion=${encodeURIComponent(occasion)}&season=${encodeURIComponent(season)}`;
  if (lockedSlots.Top) query += `&lockedTop=${lockedSlots.Top.itemId}`;
  if (lockedSlots.Bottom) query += `&lockedBottom=${lockedSlots.Bottom.itemId}`;
  if (lockedSlots.Outerwear) query += `&lockedOuter=${lockedSlots.Outerwear.itemId}`;
  if (lockedSlots.Footwear) query += `&lockedFoot=${lockedSlots.Footwear.itemId}`;
  if (lockedSlots.Accessory) query += `&lockedAcc=${lockedSlots.Accessory.itemId}`;

  const shuffleIcon = document.getElementById('shuffleIcon');
  if (shuffleIcon) shuffleIcon.style.animation = 'spin 0.4s ease';

  try {
    const res = await fetch(query);
    const data = await res.json();
    if (data.success) {
      currentOutfit = data.outfit;
      renderOutfitSlots(currentOutfit);
      renderAffiliateSuggestions(data.affiliateSuggestions || []);
      loadSavedOutfits();
    }
  } catch (e) {
    console.error("Generator error", e);
  } finally {
    if (shuffleIcon) setTimeout(() => shuffleIcon.style.animation = '', 400);
  }
}

function renderOutfitSlots(outfit) {
  renderSlot('Top', outfit.top);
  renderSlot('Bottom', outfit.bottom);
  renderSlot('Outerwear', outfit.outerwear);
  renderSlot('Footwear', outfit.footwear);
  renderSlot('Accessory', outfit.accessory);

  // Peer Suggestion Banner
  const peerWrap = document.getElementById('peerSuggestionWrap');
  if (outfit.peerSuggestedItem) {
    peerWrap.style.display = 'flex';
    document.getElementById('peerSuggestImg').src = outfit.peerSuggestedItem.imagePath;
    document.getElementById('peerSuggestDesc').textContent = outfit.peerSuggestionReason;
    activeTargetItem = outfit.peerSuggestedItem;
  } else {
    peerWrap.style.display = 'none';
  }
}

function renderSlot(slotName, item) {
  const card = document.getElementById(`slot-${slotName.toLowerCase()}`);
  const imgWrap = document.getElementById(`slot-${slotName.toLowerCase()}-img-wrap`);
  const meta = document.getElementById(`slot-${slotName.toLowerCase()}-meta`);
  const lockBtn = document.getElementById(`lock-${slotName.toLowerCase()}`);

  if (!card) return;

  if (item) {
    card.classList.add('filled');
    imgWrap.innerHTML = `<img src="${item.imagePath}" alt="${item.name}" class="slot-img" onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500'">`;
    meta.innerHTML = `
      <div class="slot-name">${item.name}</div>
      <div style="font-size:0.72rem; color:var(--text-muted);">${item.color} • ${item.style}</div>
    `;
  } else {
    card.classList.remove('filled');
    imgWrap.innerHTML = `<span class="slot-empty">No ${slotName}</span>`;
    meta.innerHTML = '';
  }

  if (lockedSlots[slotName]) {
    lockBtn.classList.add('locked');
    lockBtn.textContent = '🔒';
  } else {
    lockBtn.classList.remove('locked');
    lockBtn.textContent = '🔓';
  }
}

function toggleLock(slotName) {
  if (lockedSlots[slotName]) {
    lockedSlots[slotName] = null;
    showToast(`Unlocked ${slotName}`);
  } else if (currentOutfit && currentOutfit[slotName.toLowerCase()]) {
    lockedSlots[slotName] = currentOutfit[slotName.toLowerCase()];
    showToast(`Locked ${slotName}`);
  }
  renderSlot(slotName, currentOutfit ? currentOutfit[slotName.toLowerCase()] : null);
}

function unlockAllSlots() {
  lockedSlots = { Top: null, Bottom: null, Outerwear: null, Footwear: null, Accessory: null };
  document.querySelectorAll('.lock-btn').forEach(b => {
    b.classList.remove('locked');
    b.textContent = '🔓';
  });
  showToast("All slots unlocked.");
}

async function saveCurrentOutfit() {
  if (!currentOutfit) return;
  const name = prompt("Name this curated outfit look:", currentOutfit.name || "My Weekend Vibe");
  if (!name) return;

  try {
    const res = await fetch('/api/outfits/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: currentUserId,
        name,
        occasion: currentOutfit.occasion || 'Casual',
        topId: currentOutfit.topId || 0,
        bottomId: currentOutfit.bottomId || 0,
        outerwearId: currentOutfit.outerwearId || 0,
        footwearId: currentOutfit.footwearId || 0,
        accessoryId: currentOutfit.accessoryId || 0
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast("Look saved to your collection! ✨");
      loadSavedOutfits();
    }
  } catch (e) {
    console.error("Save outfit error", e);
  }
}

async function loadSavedOutfits() {
  try {
    const res = await fetch(`/api/outfits/saved?userId=${currentUserId}`);
    const data = await res.json();
    const container = document.getElementById('savedOutfitsList');
    if (!container) return;

    if (data.success && data.outfits && data.outfits.length > 0) {
      container.innerHTML = data.outfits.map(o => `
        <div style="background:#FFF9F7; border:1px solid var(--border); border-radius:10px; padding:0.85rem; display:flex; flex-direction:column; justify-content:space-between;">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.5rem;">
            <div>
              <div style="font-weight:700; font-size:0.88rem; color:#4B1D27;">${o.name}</div>
              <div style="font-size:0.72rem; color:var(--text-muted);">${o.occasion} • ${o.createdAt || 'Recent'}</div>
            </div>
            <button class="btn btn-outline btn-sm" onclick="deleteSavedOutfit(${o.outfitId})">🗑️</button>
          </div>
          <div style="display:flex; gap:0.4rem; overflow-x:auto; padding-top:0.4rem;">
            ${[o.top, o.bottom, o.outerwear, o.footwear, o.accessory].filter(Boolean).map(item => `
              <img src="${item.imagePath}" style="width:42px; height:42px; border-radius:6px; object-fit:cover; border:1px solid #E09A8A;" title="${item.name}">
            `).join('')}
          </div>
        </div>
      `).join('');
    } else {
      container.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No saved looks yet.</p>`;
    }
  } catch (e) {
    console.error("Error loading saved outfits", e);
  }
}

async function deleteSavedOutfit(outfitId) {
  try {
    const res = await fetch(`/api/outfits?outfitId=${outfitId}&userId=${currentUserId}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast("Look removed.");
      loadSavedOutfits();
    }
  } catch (e) {
    console.error("Delete outfit error", e);
  }
}

// AFFILIATE SUGGESTIONS
function renderAffiliateSuggestions(list) {
  const container = document.getElementById('affiliateList');
  if (!container) return;
  if (!list || list.length === 0) {
    container.innerHTML = `<p style="font-size:0.8rem; color:var(--text-muted);">No suggestions available for this vibe.</p>`;
    return;
  }

  container.innerHTML = list.map(item => `
    <div class="affiliate-card">
      <img src="${item.imageUrl}" alt="${item.title}" class="aff-img">
      <div class="aff-info">
        <span class="aff-platform ${item.platform}">${item.platform} Verified Match</span>
        <h5 class="aff-title">${item.title}</h5>
        <div style="font-size:0.7rem; color:var(--text-muted); margin-bottom:0.4rem;">${item.reason}</div>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-top:auto;">
          <span class="aff-price">${item.price}</span>
          <a href="${item.affiliateUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-outline btn-sm" style="font-size:0.72rem; padding:0.25rem 0.55rem; text-decoration:none;">
            View Store →
          </a>
        </div>
      </div>
    </div>
  `).join('');
}

// PEER LENDING HUB
async function loadLendingHub() {
  await Promise.all([loadPeerWardrobes(), loadLendingRequests()]);
}

async function loadPeerWardrobes() {
  try {
    const res = await fetch(`/api/network/peers?userId=${currentUserId}`);
    const data = await res.json();
    const container = document.getElementById('peerItemsGrid');
    if (!container) return;

    if (data.success && data.items && data.items.length > 0) {
      peerWardrobe = data.items;
      container.innerHTML = peerWardrobe.map(item => `
        <div class="item-card">
          <div class="item-img-wrap">
            <img src="${item.imagePath}" alt="${item.name}" class="item-img" onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500'">
            <span class="badge badge-avail">Owner: ${item.ownerName}</span>
          </div>
          <div class="item-details">
            <h4 class="item-name">${item.name}</h4>
            <div class="item-meta">
              <span class="tag-pill">${item.category}</span>
              <span class="tag-pill">${item.size}</span>
              <span class="tag-pill">${item.style}</span>
            </div>
            <div class="item-actions">
              <button class="btn btn-primary btn-sm" style="width:100%;" onclick="openBorrowModalForItem(${item.itemId})">
                🤝 Request to Borrow
              </button>
            </div>
          </div>
        </div>
      `).join('');
    } else {
      container.innerHTML = `<p style="grid-column:1/-1; text-align:center; padding:2rem; color:var(--text-muted);">No peer items currently available.</p>`;
    }
  } catch (e) {
    console.error("Error loading peer wardrobes", e);
  }
}

async function loadLendingRequests() {
  try {
    const res = await fetch(`/api/lending/requests?userId=${currentUserId}`);
    const data = await res.json();

    const incomingContainer = document.getElementById('incomingRequestsList');
    const outgoingContainer = document.getElementById('outgoingRequestsList');

    if (incomingContainer) {
      const incoming = data.incoming || [];
      if (incoming.length === 0) {
        incomingContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No incoming requests.</p>`;
      } else {
        incomingContainer.innerHTML = incoming.map(r => `
          <div style="background:#FFF9F7; border:1px solid var(--border); border-radius:10px; padding:0.85rem; margin-bottom:0.75rem;">
            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
              <div style="display:flex; gap:0.65rem;">
                <img src="${r.itemImagePath}" style="width:50px; height:50px; border-radius:8px; object-fit:cover;">
                <div>
                  <div style="font-weight:700; font-size:0.88rem;">${r.itemName}</div>
                  <div style="font-size:0.75rem; color:var(--text-muted);">Requested by <strong>${r.requesterName}</strong> • Return by: ${r.returnDate || '3 days'}</div>
                  <div style="font-size:0.75rem; color:var(--text-muted); margin-top:2px;">"${r.requestNotes || ''}"</div>
                </div>
              </div>
              <span class="status-badge status-${r.status}">${r.status}</span>
            </div>
            <div style="display:flex; gap:0.5rem; justify-content:flex-end; margin-top:0.75rem;">
              ${r.status === 'PENDING' ? `
                <button class="btn btn-outline btn-sm" onclick="handleLendingAction(${r.requestId}, 'reject')">Decline</button>
                <button class="btn btn-primary btn-sm" onclick="handleLendingAction(${r.requestId}, 'approve')">Approve Request</button>
              ` : ''}
              ${r.status === 'APPROVED' ? `
                <button class="btn btn-success btn-sm" onclick="openHandoverModal(${r.requestId}, '${r.itemImagePath}')">
                  📸 Record Pre-Lend Photo & Handover
                </button>
              ` : ''}
              ${r.status === 'RETURN_REQUESTED' ? `
                <button class="btn btn-primary btn-sm" onclick="viewReturnInspection(${r.requestId})">
                  🔍 Review AI Verification & Rate
                </button>
              ` : ''}
            </div>
          </div>
        `).join('');
      }
    }

    if (outgoingContainer) {
      const outgoing = data.outgoing || [];
      if (outgoing.length === 0) {
        outgoingContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No outgoing requests.</p>`;
      } else {
        outgoingContainer.innerHTML = outgoing.map(r => `
          <div style="background:#FFF9F7; border:1px solid var(--border); border-radius:10px; padding:0.85rem; margin-bottom:0.75rem;">
            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
              <div style="display:flex; gap:0.65rem;">
                <img src="${r.itemImagePath}" style="width:50px; height:50px; border-radius:8px; object-fit:cover;">
                <div>
                  <div style="font-weight:700; font-size:0.88rem;">${r.itemName}</div>
                  <div style="font-size:0.75rem; color:var(--text-muted);">Borrowed from <strong>${r.ownerName}</strong> • ${r.returnDate || '3 days'}</div>
                </div>
              </div>
              <span class="status-badge status-${r.status}">${r.status}</span>
            </div>
            <div style="display:flex; gap:0.5rem; justify-content:flex-end; margin-top:0.75rem;">
              ${r.status === 'HANDED_OVER' ? `
                <button class="btn btn-primary btn-sm" onclick="openReturnInspectModal(${r.requestId}, '${r.preLendImage || r.itemImagePath}')">
                  📸 Return Item & AI Verification
                </button>
              ` : ''}
              ${r.status === 'RETURN_REQUESTED' ? `
                <span style="font-size:0.78rem; color:#6D2E3A; font-weight:600;">✓ AI Check Submitted (Score: ${r.aiVerificationScore}%)</span>
              ` : ''}
            </div>
          </div>
        `).join('');
      }
    }

  } catch (e) {
    console.error("Error loading lending requests", e);
  }
}

async function handleLendingAction(requestId, action) {
  try {
    const res = await fetch(`/api/lending/${action}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ requestId })
    });
    const data = await res.json();
    if (data.success) {
      showToast(`Request ${action}d successfully.`);
      loadLendingRequests();
    }
  } catch (e) {
    console.error("Error updating lending action", e);
  }
}

function openBorrowModalForItem(itemId) {
  const item = peerWardrobe.find(i => i.itemId === itemId);
  if (!item) return;
  activeTargetItem = item;
  document.getElementById('borrowModalImg').src = item.imagePath;
  document.getElementById('borrowModalName').textContent = item.name;
  document.getElementById('borrowModalOwner').textContent = `Owner: ${item.ownerName}`;
  openModal('modalBorrowRequest');
}

function borrowSuggestedItem() {
  if (activeTargetItem) {
    openBorrowModalForItem(activeTargetItem.itemId);
  }
}

async function submitBorrowRequest() {
  if (!activeTargetItem) return;
  const returnDate = document.getElementById('borrowReturnDate').value || 'In 3 Days';
  const notes = document.getElementById('borrowNotes').value || 'Would love to borrow this piece!';

  try {
    const res = await fetch('/api/lending/request', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        itemId: activeTargetItem.itemId,
        userId: currentUserId,
        returnDate,
        notes
      })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalBorrowRequest');
      showToast("Borrow request sent to peer! 🤝");
      loadLendingHub();
    }
  } catch (e) {
    console.error("Error submitting borrow request", e);
  }
}

// HANDOVER & RETURN AI INSPECTION
function openHandoverModal(requestId, itemImagePath) {
  activeHandoverReqId = requestId;
  document.getElementById('handoverPhotoUrl').value = itemImagePath || '';
  openModal('modalHandover');
}

function useExistingItemPhotoAsBaseline() {
  showToast("Baseline photo locked from wardrobe profile.");
}

async function confirmHandover() {
  if (!activeHandoverReqId) return;
  const preLendImage = document.getElementById('handoverPhotoUrl').value;
  try {
    const res = await fetch('/api/lending/handover', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ requestId: activeHandoverReqId, preLendImage })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalHandover');
      showToast("Item handover confirmed & baseline recorded! 📸");
      loadLendingHub();
    }
  } catch (e) {
    console.error("Handover error", e);
  }
}

function openReturnInspectModal(requestId, baselinePhoto) {
  activeReturnReqId = requestId;
  document.getElementById('returnPhotoUrl').value = baselinePhoto;
  openModal('modalReturnInspect');
}

function setReturnDemoPhoto(type) {
  if (type === 'clean') {
    document.getElementById('returnPhotoUrl').value = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500';
    showToast("Clean return photo set (Pristine 98% Match)");
  } else {
    document.getElementById('returnPhotoUrl').value = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&damage=stain';
    showToast("Stained condition photo set (Will flag damage 63% Match)");
  }
}

async function submitReturnInspection() {
  if (!activeReturnReqId) return;
  const postLendImage = document.getElementById('returnPhotoUrl').value;
  try {
    const res = await fetch('/api/lending/return-inspect', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ requestId: activeReturnReqId, postLendImage })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalReturnInspect');
      showToast("AI Verification complete! Condition analyzed. ✨");
      loadLendingHub();
      // Jump to verification tab to review results
      switchTab('verification');
      renderVerificationView(data.aiVerification);
    }
  } catch (e) {
    console.error("Return inspection error", e);
  }
}

function viewReturnInspection(requestId) {
  switchTab('verification');
  loadVerificationDemoPreset('pristine');
}

// AI VERIFICATION LAB
function loadVerificationDemoPreset(type) {
  const preImg = document.getElementById('inspectPreLendImg');
  const postImg = document.getElementById('inspectPostLendImg');
  const banner = document.getElementById('aiScoreBanner');
  const title = document.getElementById('aiVerdictTitle');
  const desc = document.getElementById('aiVerdictDesc');
  const num = document.getElementById('aiScoreNumber');

  if (type === 'pristine') {
    preImg.src = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500';
    postImg.src = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500';
    banner.className = 'score-banner passed';
    title.textContent = 'VERIFIED INTACT (98.2% Match)';
    desc.textContent = 'Zero stains, zero fabric abrasions, zipper hardware 100% functional. Eligible for full trust score rating.';
    num.textContent = '98.2%';
  } else {
    preImg.src = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500';
    postImg.src = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&damage=stain';
    banner.className = 'score-banner flagged';
    title.textContent = 'FLAGGED DAMAGE (63.5% Match)';
    desc.textContent = 'AI Verification Flagged Mismatch: Detected surface discoloration and localized fabric abrasion compared to pre-lend baseline. Estimated compensation: ₹400.';
    num.textContent = '63.5%';
  }
}

function renderVerificationView(result) {
  if (!result) return;
  const banner = document.getElementById('aiScoreBanner');
  const title = document.getElementById('aiVerdictTitle');
  const desc = document.getElementById('aiVerdictDesc');
  const num = document.getElementById('aiScoreNumber');

  if (result.status === 'PASSED') {
    banner.className = 'score-banner passed';
    title.textContent = `VERIFIED INTACT (${result.matchScore}% Match)`;
  } else {
    banner.className = 'score-banner flagged';
    title.textContent = `FLAGGED DAMAGE (${result.matchScore}% Match)`;
  }
  desc.textContent = result.summaryReport;
  num.textContent = `${result.matchScore}%`;
}

function proceedToRatingFromVerification() {
  openModal('modalRate');
}

function openDisputeModalFromVerification() {
  openCreateComplaintModal();
}

// RATINGS & TRUST
let selectedRating = 5;
function setRatingScore(stars) {
  selectedRating = stars;
  document.getElementById('selectedStarScore').value = stars;
  const spans = document.querySelectorAll('#starRatingSelect span');
  spans.forEach((s, idx) => {
    s.style.color = idx < stars ? '#C86A5A' : '#E09A8A';
  });
}

async function submitRating() {
  const rating = selectedRating;
  const review = document.getElementById('rateReviewText').value || 'Smooth lending exchange!';
  const aspectTags = document.getElementById('rateAspectTags').value || 'Clean, Punctual';

  try {
    const res = await fetch('/api/lending/complete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        requestId: 1, // sample or active
        borrowerRating: rating,
        lenderRating: 5,
        review,
        aspectTags
      })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalRate');
      showToast("Rating submitted! Peer trust recalculated ⭐");
      refreshActiveUserData();
      switchTab('ratings');
    }
  } catch (e) {
    console.error("Error submitting rating", e);
  }
}

async function loadRatingsWall() {
  try {
    const res = await fetch(`/api/ratings?userId=${currentUserId}`);
    const data = await res.json();
    const container = document.getElementById('ratingsList');
    if (!container) return;

    if (data.success && data.ratings && data.ratings.length > 0) {
      container.innerHTML = data.ratings.map(r => `
        <div style="background:#FFF9F7; border:1px solid var(--border); border-radius:10px; padding:1rem;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.35rem;">
            <div style="font-weight:700; font-size:0.9rem; color:#4B1D27;">${r.raterName} • ${r.itemName}</div>
            <div style="color:#C86A5A; font-weight:700;">${'★'.repeat(r.score)}${'☆'.repeat(5 - r.score)}</div>
          </div>
          <p style="font-size:0.84rem; color:#7E565F; line-height:1.4; margin-bottom:0.5rem;">"${r.review}"</p>
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span class="tag-pill" style="background:#FDEDE7; color:#6D2E3A; font-size:0.72rem;">${r.aspectTags || 'Punctual'}</span>
            <span style="font-size:0.72rem; color:var(--text-muted);">${r.createdAt || 'Recent'}</span>
          </div>
        </div>
      `).join('');
    } else {
      container.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No reviews yet. Complete your first peer exchange to earn ratings!</p>`;
    }
  } catch (e) {
    console.error("Ratings error", e);
  }
}

// COMPLAINTS & COMPENSATION MODULE
async function loadComplaints() {
  try {
    const res = await fetch(`/api/complaints?userId=${currentUserId}`);
    const data = await res.json();
    const tbody = document.getElementById('complaintsTableBody');
    if (!tbody) return;

    if (data.success && data.complaints && data.complaints.length > 0) {
      tbody.innerHTML = data.complaints.map(c => `
        <tr>
          <td><strong>#CMP-${c.complaintId}</strong></td>
          <td>
            <div style="font-weight:700; color:#4B1D27;">${c.itemName}</div>
            <div style="font-size:0.75rem; color:var(--text-muted);">${c.complaintType}</div>
          </td>
          <td>${c.complainantName}</td>
          <td>${c.respondentName}</td>
          <td>
            <span style="font-weight:700; color:#9A4454;">${c.aiSeverityScore ? c.aiSeverityScore.toFixed(1) + '%' : 'N/A'}</span>
          </td>
          <td>₹${c.claimedAmount}</td>
          <td><strong style="color:#6D2E3A;">${c.settledAmount > 0 ? '₹' + c.settledAmount : 'Pending'}</strong></td>
          <td><span class="status-badge status-${c.status}">${c.status}</span></td>
          <td>
            ${c.status !== 'SETTLED' ? `
              <button class="btn btn-primary btn-sm" onclick="resolveComplaintModal(${c.complaintId}, ${c.claimedAmount})">
                Resolve
              </button>
            ` : `<span style="font-size:0.75rem; color:#6D2E3A; font-weight:700;">✓ Settled</span>`}
          </td>
        </tr>
      `).join('');
    } else {
      tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding:2rem; color:var(--text-muted);">No open complaints. Peer exchanges are running smoothly!</td></tr>`;
    }
  } catch (e) {
    console.error("Complaints error", e);
  }
}

function openCreateComplaintModal() {
  updateEstimatedCompensation();
  openModal('modalComplaint');
}

function updateEstimatedCompensation() {
  const type = document.getElementById('compType')?.value || 'STAINS';
  let est = 450;
  let text = 'Stain removal / dry-cleaning reimbursement formula.';
  if (type === 'TEARS') {
    est = 650;
    text = 'Seam stitching and fabric restoration repair estimate.';
  } else if (type === 'ITEM_LOST') {
    est = 1800;
    text = '85% depreciated retail replacement compensation.';
  } else if (type === 'LATE_RETURN') {
    est = 200;
    text = 'Peer delay compensation standard rate.';
  }
  document.getElementById('compAiEstimate').textContent = `₹${est}`;
  document.getElementById('compClaimedAmount').value = est;
  document.getElementById('compAiExplanation').textContent = text;
}

async function submitComplaint() {
  const complaintType = document.getElementById('compType').value;
  const description = document.getElementById('compDescription').value || 'Damage flagged during return check.';
  const evidenceImage = document.getElementById('compEvidenceImg').value;
  const claimedAmount = parseFloat(document.getElementById('compClaimedAmount').value) || 450;

  try {
    const res = await fetch('/api/complaints/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        requestId: 1,
        itemId: 1,
        complainantId: currentUserId,
        respondentId: currentUserId === 1 ? 2 : 1,
        complaintType,
        description,
        evidenceImage,
        claimedAmount,
        aiSeverityScore: 28.5
      })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalComplaint');
      showToast("Dispute ticket filed with AI condition evidence. ⚖️");
      loadComplaints();
    }
  } catch (e) {
    console.error("Complaint error", e);
  }
}

async function resolveComplaintModal(complaintId, defaultAmount) {
  const settled = prompt("Enter agreed settlement compensation in INR (₹):", defaultAmount || 400);
  if (!settled || isNaN(settled)) return;

  try {
    const res = await fetch('/api/complaints/resolve', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        complaintId,
        status: 'SETTLED',
        settledAmount: parseFloat(settled),
        resolutionNotes: `Mutual peer settlement concluded: ₹${settled} compensated.`
      })
    });
    const data = await res.json();
    if (data.success) {
      showToast(`Dispute #${complaintId} marked settled! ✨`);
      loadComplaints();
    }
  } catch (e) {
    console.error("Resolution error", e);
  }
}

// THRIFT STORE
async function loadThriftStore() {
  try {
    const res = await fetch('/api/thrift/items');
    const data = await res.json();
    const container = document.getElementById('thriftGrid');
    if (!container) return;

    if (data.success && data.items && data.items.length > 0) {
      container.innerHTML = data.items.map(item => `
        <div class="item-card">
          <div class="item-img-wrap">
            <img src="${item.imagePath}" alt="${item.name}" class="item-img" onerror="this.src='https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500'">
            <span class="badge badge-avail" style="background:#6D2E3A;">Seller: ${item.sellerName}</span>
            <span class="badge-thrift">₹${item.price}</span>
          </div>
          <div class="item-details">
            <h4 class="item-name">${item.name}</h4>
            <div class="item-meta">
              <span class="tag-pill">${item.category}</span>
              <span class="tag-pill">${item.size}</span>
              <span class="tag-pill">Original: ₹${item.originalValue}</span>
              <span class="tag-pill" style="color:#C86A5A;">★ ${item.sellerTrustScore.toFixed(1)}</span>
            </div>
            <p style="font-size:0.78rem; color:var(--text-muted); margin-bottom:0.85rem;">${item.description}</p>
            <div class="item-actions">
              ${item.sellerId !== currentUserId ? `
                <button class="btn btn-success btn-sm" style="width:100%;" onclick="buyThriftItem(${item.itemId}, ${item.price})">
                  💳 Buy for ₹${item.price}
                </button>
              ` : `
                <button class="btn btn-outline btn-sm" style="width:100%;" disabled>Your Listing</button>
              `}
            </div>
          </div>
        </div>
      `).join('');
    } else {
      container.innerHTML = `<p style="grid-column:1/-1; text-align:center; padding:3rem; color:var(--text-muted);">No items currently in the thrift store. List one from your wardrobe!</p>`;
    }
  } catch (e) {
    console.error("Thrift load error", e);
  }
}

async function buyThriftItem(itemId, price) {
  if (!confirm(`Confirm purchase of this item for ₹${price}? It will immediately be transferred into your digital wardrobe.`)) return;
  try {
    const res = await fetch('/api/thrift/buy', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemId, buyerId: currentUserId })
    });
    const data = await res.json();
    if (data.success) {
      showToast("Purchase successful! Item transferred to your digital wardrobe. 🎉");
      loadThriftStore();
      loadWardrobeData();
    }
  } catch (e) {
    console.error("Buy error", e);
  }
}

// ADD ITEM MODAL
function openAddItemModal(forThrift = false) {
  document.getElementById('newItemIsThrift').value = forThrift ? 'true' : 'false';
  toggleThriftPriceInput();
  openModal('modalAddItem');
}

function toggleThriftPriceInput() {
  const isThrift = document.getElementById('newItemIsThrift').value === 'true';
  document.getElementById('thriftPriceGroup').style.display = isThrift ? 'block' : 'none';
}

function setSampleImg(type) {
  const input = document.getElementById('newItemImageUrl');
  if (type === 'jacket') input.value = 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500';
  else if (type === 'sunglasses') input.value = 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500';
  else if (type === 'sneakers') input.value = 'https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=500';
  else if (type === 'trousers') input.value = 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=500';
}

async function submitNewItem() {
  const name = document.getElementById('newItemName').value.trim();
  if (!name) {
    alert("Please enter item name.");
    return;
  }
  const category = document.getElementById('newItemCategory').value;
  const subcategory = document.getElementById('newItemSubcategory').value || category;
  const size = document.getElementById('newItemSize').value || 'M';
  const color = document.getElementById('newItemColor').value || 'Neutral';
  const style = document.getElementById('newItemStyle').value || 'Casual';
  const imagePath = document.getElementById('newItemImageUrl').value || 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=500';
  const originalValue = parseFloat(document.getElementById('newItemOriginalVal').value) || 2000;
  const isForSale = document.getElementById('newItemIsThrift').value === 'true';
  const price = isForSale ? (parseFloat(document.getElementById('newItemThriftPrice').value) || 499) : 0;

  try {
    const res = await fetch('/api/wardrobe', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: currentUserId,
        name,
        category,
        subcategory,
        color,
        style,
        size,
        imagePath,
        originalValue,
        isForSale,
        price
      })
    });
    const data = await res.json();
    if (data.success) {
      closeModal('modalAddItem');
      showToast("Piece added to your digital wardrobe! ✨");
      loadWardrobeData();
    }
  } catch (e) {
    console.error("Add item error", e);
  }
}

// MODAL UTILITIES
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('open');
}

function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('open');
}

// TOAST NOTIFIER
let toastTimer = null;
function showToast(msg) {
  const el = document.getElementById('toast');
  if (!el) return;
  el.textContent = msg;
  el.style.display = 'block';
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    el.style.display = 'none';
  }, 3200);
}
