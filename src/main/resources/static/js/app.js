/**
 * PortfolioPro - Trading & Portfolio Management Engine Frontend
 */

const API_BASE = ''; // Same origin (port 8080)

// Application State
let currentSymbol = 'AAPL';
let currentOrderType = 'BUY';
let currentChartMode = 'price'; // 'price' | 'rsi' | 'macd'
let stockQuotes = [];
let selectedQuote = null;
let historicalBars = [];
let technicalData = null;
let marketChartInstance = null;
let allocationChartInstance = null;
let portfolioSummaryData = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    initEventListeners();
    fetchAllData();
    // Live market tick interval (every 4 seconds)
    setInterval(refreshMarketQuotes, 4000);
});

function initEventListeners() {
    // Chart tab toggles
    document.querySelectorAll('.chart-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.chart-tab').forEach(t => t.classList.remove('active'));
            e.target.classList.add('active');
            currentChartMode = e.target.getAttribute('data-chart');
            renderMarketChart();
        });
    });

    // Technical / Fundamental Analysis tab toggles
    document.querySelectorAll('.analysis-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.analysis-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
            e.currentTarget.classList.add('active');
            const targetId = e.currentTarget.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });

    // Search input
    const searchInput = document.getElementById('tickerSearchInput');
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            const sym = searchInput.value.trim().toUpperCase();
            if (sym) {
                selectStock(sym);
                searchInput.value = '';
            }
        }
    });

    // Reset portfolio
    document.getElementById('btnResetPortfolio').addEventListener('click', resetPortfolio);

    // Deposit modal handlers
    document.getElementById('btnDepositModal').addEventListener('click', () => {
        document.getElementById('depositModal').classList.add('active');
    });
}

// ----------------- Data Fetching -----------------

async function fetchAllData() {
    await refreshMarketQuotes();
    await selectStock(currentSymbol);
    await refreshPortfolioAndRisk();
    await loadTransactionHistory();
}

async function refreshMarketQuotes() {
    try {
        const res = await fetch(`${API_BASE}/api/market/quotes`);
        if (!res.ok) return;
        stockQuotes = await res.json();
        renderTickerRibbon();
        renderStockPills();

        // Update selected quote if present
        if (currentSymbol) {
            const updated = stockQuotes.find(q => q.symbol === currentSymbol);
            if (updated) {
                selectedQuote = updated;
                updateStockBanner();
                calculateOrderEstimate();
            }
        }

        // Periodically refresh holdings to reflect live prices
        refreshPortfolioAndRisk(false);
    } catch (err) {
        console.error('Error fetching market quotes:', err);
    }
}

async function selectStock(symbol) {
    currentSymbol = symbol.toUpperCase();
    try {
        // Fetch quote, history, technical analysis, fundamentals concurrently
        const [qRes, hRes, tRes, fRes] = await Promise.all([
            fetch(`${API_BASE}/api/market/quote/${currentSymbol}`),
            fetch(`${API_BASE}/api/market/history/${currentSymbol}`),
            fetch(`${API_BASE}/api/market/analysis/${currentSymbol}`),
            fetch(`${API_BASE}/api/market/fundamental/${currentSymbol}`)
        ]);

        if (qRes.ok) selectedQuote = await qRes.json();
        if (hRes.ok) historicalBars = await hRes.json();
        if (tRes.ok) technicalData = await tRes.json();

        updateStockBanner();
        renderMarketChart();
        renderTechnicalAnalysis();

        if (fRes.ok) {
            const fund = await fRes.json();
            renderFundamentalAnalysis(fund);
        }

        // Update order form
        document.getElementById('orderSymbol').value = currentSymbol;
        calculateOrderEstimate();
        highlightActivePill();
    } catch (err) {
        console.error('Error selecting stock:', err);
    }
}

async function refreshPortfolioAndRisk(refreshCharts = true) {
    try {
        const [summaryRes, riskRes, allocRes] = await Promise.all([
            fetch(`${API_BASE}/api/portfolio/summary`),
            fetch(`${API_BASE}/api/portfolio/risk`),
            fetch(`${API_BASE}/api/portfolio/allocations`)
        ]);

        if (summaryRes.ok) {
            portfolioSummaryData = await summaryRes.json();
            renderPortfolioSummary(portfolioSummaryData);
            renderHoldingsTable(portfolioSummaryData.positions || []);
        }

        if (riskRes.ok) {
            const risk = await riskRes.json();
            renderRiskMetrics(risk);
        }

        if (allocRes.ok && refreshCharts) {
            const allocs = await allocRes.json();
            renderAllocationChart(allocs);
        }
    } catch (err) {
        console.error('Error refreshing portfolio:', err);
    }
}

async function loadTransactionHistory() {
    try {
        const res = await fetch(`${API_BASE}/api/trade/history`);
        if (!res.ok) return;
        const orders = await res.json();
        renderHistoryTable(orders);
    } catch (err) {
        console.error('Error loading trade history:', err);
    }
}

// ----------------- UI Rendering -----------------

function renderTickerRibbon() {
    const container = document.getElementById('ribbonContent');
    if (!container) return;
    
    // Duplicate list for seamless infinite ribbon scroll
    const items = [...stockQuotes, ...stockQuotes];
    container.innerHTML = items.map(q => {
        const isUp = q.change >= 0;
        const colClass = isUp ? 'text-green' : 'text-red';
        const sign = isUp ? '+' : '';
        return `
            <div class="ticker-chip" onclick="selectStock('${q.symbol}')">
                <span class="sym">${q.symbol}</span>
                <span class="price">$${q.currentPrice.toFixed(2)}</span>
                <span class="change ${colClass}">${sign}${q.changePercent.toFixed(2)}%</span>
            </div>
        `;
    }).join('');
}

function renderStockPills() {
    const container = document.getElementById('stockPills');
    if (!container || container.children.length > 0) return; // Build once

    container.innerHTML = stockQuotes.map(q => `
        <button class="stock-pill ${q.symbol === currentSymbol ? 'active' : ''}" onclick="selectStock('${q.symbol}')" id="pill-${q.symbol}">
            ${q.symbol}
        </button>
    `).join('');
}

function highlightActivePill() {
    document.querySelectorAll('.stock-pill').forEach(pill => {
        pill.classList.remove('active');
    });
    const active = document.getElementById(`pill-${currentSymbol}`);
    if (active) active.classList.add('active');
}

function updateStockBanner() {
    if (!selectedQuote) return;
    const q = selectedQuote;
    const isUp = q.change >= 0;
    const sign = isUp ? '+' : '';
    const colorClass = isUp ? 'text-green' : 'text-red';

    document.getElementById('displaySymbol').textContent = q.symbol;
    document.getElementById('displaySector').textContent = q.sector || 'Equities';
    document.getElementById('displayCompanyName').textContent = q.companyName;
    document.getElementById('displayPrice').textContent = `$${q.currentPrice.toFixed(2)}`;

    document.getElementById('displayDelta').innerHTML = `
        <span class="change-tag ${colorClass}">${sign}$${q.change.toFixed(2)} (${sign}${q.changePercent.toFixed(2)}%) Today</span>
    `;

    document.getElementById('displayHigh').textContent = `$${q.dayHigh.toFixed(2)}`;
    document.getElementById('displayLow').textContent = `$${q.dayLow.toFixed(2)}`;
    document.getElementById('displayVolume').textContent = (q.volume / 1000000).toFixed(2) + 'M';
    document.getElementById('displayPrevClose').textContent = `$${q.previousClose.toFixed(2)}`;

    document.getElementById('orderPrice').value = q.currentPrice.toFixed(2);
}

// ----------------- Chart.js Engine -----------------

function renderMarketChart() {
    const canvas = document.getElementById('marketChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    if (marketChartInstance) {
        marketChartInstance.destroy();
    }

    if (!historicalBars || historicalBars.length === 0) return;

    const labels = historicalBars.map(b => b.date.slice(5)); // MM-DD
    const closePrices = historicalBars.map(b => b.close);

    let datasets = [];

    if (currentChartMode === 'price') {
        const isBullish = closePrices[closePrices.length - 1] >= closePrices[0];
        const lineColor = isBullish ? '#10b981' : '#ef4444';
        const fillColor = isBullish ? 'rgba(16, 185, 129, 0.08)' : 'rgba(239, 68, 68, 0.08)';

        datasets.push({
            label: `${currentSymbol} Price`,
            data: closePrices,
            borderColor: lineColor,
            backgroundColor: fillColor,
            fill: true,
            tension: 0.25,
            borderWidth: 2,
            pointRadius: 1,
            pointHoverRadius: 6
        });

        if (technicalData && technicalData.sma20Series) {
            datasets.push({
                label: 'SMA (20)',
                data: technicalData.sma20Series,
                borderColor: '#3b82f6',
                borderWidth: 1.5,
                borderDash: [4, 4],
                fill: false,
                pointRadius: 0
            });
        }

        if (technicalData && technicalData.bollingerUpper && technicalData.bollingerLower) {
            datasets.push({
                label: 'Bollinger Upper',
                data: Array(closePrices.length).fill(technicalData.bollingerUpper),
                borderColor: 'rgba(139, 92, 246, 0.4)',
                borderWidth: 1,
                borderDash: [2, 2],
                fill: false,
                pointRadius: 0
            });
            datasets.push({
                label: 'Bollinger Lower',
                data: Array(closePrices.length).fill(technicalData.bollingerLower),
                borderColor: 'rgba(139, 92, 246, 0.4)',
                borderWidth: 1,
                borderDash: [2, 2],
                fill: false,
                pointRadius: 0
            });
        }
    } else if (currentChartMode === 'rsi') {
        const rsiSeries = technicalData && technicalData.rsiSeries ? technicalData.rsiSeries : closePrices.map(() => 50);
        datasets.push({
            label: 'RSI (14)',
            data: rsiSeries,
            borderColor: '#8b5cf6',
            backgroundColor: 'rgba(139, 92, 246, 0.1)',
            fill: true,
            tension: 0.2,
            borderWidth: 2,
            pointRadius: 1
        });
        datasets.push({
            label: 'Overbought (70)',
            data: Array(labels.length).fill(70),
            borderColor: 'rgba(239, 68, 68, 0.6)',
            borderWidth: 1,
            borderDash: [5, 5],
            fill: false,
            pointRadius: 0
        });
        datasets.push({
            label: 'Oversold (30)',
            data: Array(labels.length).fill(30),
            borderColor: 'rgba(16, 185, 129, 0.6)',
            borderWidth: 1,
            borderDash: [5, 5],
            fill: false,
            pointRadius: 0
        });
    } else if (currentChartMode === 'macd') {
        const macdVal = technicalData ? technicalData.macd : 0;
        const macdSig = technicalData ? technicalData.macdSignal : 0;
        const macdHist = technicalData ? technicalData.macdHistogram : 0;

        datasets.push({
            label: 'MACD (12, 26)',
            data: Array(labels.length).fill(macdVal),
            borderColor: '#3b82f6',
            borderWidth: 2,
            fill: false,
            pointRadius: 0
        });
        datasets.push({
            label: 'Signal Line (9)',
            data: Array(labels.length).fill(macdSig),
            borderColor: '#f59e0b',
            borderWidth: 1.5,
            borderDash: [4, 4],
            fill: false,
            pointRadius: 0
        });
    }

    marketChartInstance = new Chart(ctx, {
        type: 'line',
        data: { labels, datasets },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: {
                    labels: { color: '#94a3b8', font: { family: 'Plus Jakarta Sans', size: 11 } }
                },
                tooltip: {
                    backgroundColor: '#1e293b',
                    titleColor: '#f8fafc',
                    bodyColor: '#cbd5e1',
                    borderColor: '#334155',
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    grid: { color: '#1e293b' },
                    ticks: { color: '#64748b', font: { family: 'JetBrains Mono', size: 10 } }
                },
                y: {
                    grid: { color: '#1e293b' },
                    ticks: {
                        color: '#64748b',
                        font: { family: 'JetBrains Mono', size: 10 },
                        callback: val => currentChartMode === 'price' ? `$${val}` : val
                    }
                }
            }
        }
    });
}

function renderAllocationChart(allocations) {
    const canvas = document.getElementById('allocationChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    if (allocationChartInstance) {
        allocationChartInstance.destroy();
    }

    if (!allocations || allocations.length === 0) return;

    allocationChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: allocations.map(a => a.label),
            datasets: [{
                data: allocations.map(a => a.percentage),
                backgroundColor: allocations.map(a => a.color),
                borderColor: '#121826',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '65%',
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: '#94a3b8',
                        boxWidth: 12,
                        font: { family: 'Plus Jakarta Sans', size: 10 }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: ${ctx.raw.toFixed(1)}%`
                    }
                }
            }
        }
    });
}

// ----------------- Technical & Fundamental Rendering -----------------

function renderTechnicalAnalysis() {
    if (!technicalData) return;
    const t = technicalData;

    const badge = document.getElementById('techSignalBadge');
    badge.textContent = t.signal.replace('_', ' ');
    badge.className = 'signal-badge';

    if (t.signal.includes('BUY')) badge.classList.add(t.signal.toLowerCase().replace('_', '-'));
    else if (t.signal.includes('SELL')) badge.classList.add(t.signal.toLowerCase().replace('_', '-'));
    else badge.classList.add('neutral');

    document.getElementById('techSignalSummary').textContent =
        `RSI: ${t.rsi14} | MACD Hist: ${t.macdHistogram > 0 ? '+' : ''}${t.macdHistogram.toFixed(2)}`;

    document.getElementById('techRsi').textContent = t.rsi14.toFixed(1);
    document.getElementById('techRsiDesc').textContent = t.rsiSignal || '';

    document.getElementById('techMacd').textContent = `${t.macd.toFixed(2)} / Sig: ${t.macdSignal.toFixed(2)}`;
    document.getElementById('techMacdDesc').textContent = t.macdSignalInterpretation || '';

    document.getElementById('techSma').textContent = `$${t.sma20.toFixed(2)} / $${t.sma50.toFixed(2)}`;
    document.getElementById('techBollinger').textContent = `$${t.bollingerLower.toFixed(2)} - $${t.bollingerUpper.toFixed(2)}`;
}

function renderFundamentalAnalysis(f) {
    document.getElementById('fundMarketCap').textContent = f.marketCap;
    document.getElementById('fundPe').textContent = f.peRatio.toFixed(2);
    document.getElementById('fundForwardPe').textContent = f.forwardPe.toFixed(2);
    document.getElementById('fundEps').textContent = `$${f.eps.toFixed(2)}`;
    document.getElementById('fundDividend').textContent = `${f.dividendYield.toFixed(2)}%`;
    document.getElementById('fund52w').textContent = `$${f.fiftyTwoWeekLow.toFixed(2)} - $${f.fiftyTwoWeekHigh.toFixed(2)}`;
    document.getElementById('fundBeta').textContent = f.beta.toFixed(2);
    document.getElementById('fundRating').textContent = `${f.analystRating} (Target: $${f.targetPrice.toFixed(2)})`;
    document.getElementById('fundDesc').textContent = f.description;
}

// ----------------- Portfolio & Risk Rendering -----------------

function renderPortfolioSummary(s) {
    document.getElementById('navCashBalance').textContent = `$${s.cashBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('navTotalNav').textContent = `$${s.totalPortfolioValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

    document.getElementById('cardTotalVal').textContent = `$${s.totalPortfolioValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

    const isDayUp = s.dailyPnL >= 0;
    const daySign = isDayUp ? '+' : '';
    const dayClass = isDayUp ? 'text-green' : 'text-red';
    document.getElementById('cardDailyPnL').innerHTML = `
        Day P&L: <span class="badge ${dayClass}">${daySign}$${s.dailyPnL.toFixed(2)} (${daySign}${s.dailyPnLPercent.toFixed(2)}%)</span>
    `;

    const isUnrealizedUp = s.totalUnrealizedPnL >= 0;
    const unSign = isUnrealizedUp ? '+' : '';
    const unClass = isUnrealizedUp ? 'text-green' : 'text-red';
    document.getElementById('cardUnrealizedPnL').textContent = `${unSign}$${Math.abs(s.totalUnrealizedPnL).toFixed(2)}`;
    document.getElementById('cardUnrealizedPnL').className = `metric-value ${unClass}`;
    document.getElementById('cardUnrealizedPct').innerHTML = `
        Return: <span class="badge ${unClass}">${unSign}${s.totalUnrealizedPnLPercent.toFixed(2)}%</span>
    `;

    const isRealizedUp = s.totalRealizedPnL >= 0;
    const realSign = isRealizedUp ? '+' : '';
    const realClass = isRealizedUp ? 'text-green' : 'text-red';
    document.getElementById('cardRealizedPnL').textContent = `${realSign}$${Math.abs(s.totalRealizedPnL).toFixed(2)}`;
    document.getElementById('cardRealizedPnL').className = `metric-value ${realClass}`;
}

function renderRiskMetrics(r) {
    document.getElementById('cardRiskLevel').textContent = r.riskLevel;
    document.getElementById('cardSharpe').innerHTML = `Sharpe: <strong class="text-white">${r.sharpeRatio.toFixed(2)}</strong> | Beta: <strong class="text-white">${r.portfolioBeta.toFixed(2)}</strong>`;

    document.getElementById('riskSharpe').textContent = r.sharpeRatio.toFixed(2);
    document.getElementById('riskBeta').textContent = r.portfolioBeta.toFixed(2);
    document.getElementById('riskVarDollar').textContent = `$${r.var95Dollar.toFixed(2)} (${r.var95Percent.toFixed(1)}%)`;
    document.getElementById('riskVolatility').textContent = `${r.annualizedVolatility.toFixed(1)}% / yr`;
    document.getElementById('riskDrawdown').textContent = `-${r.maxDrawdown.toFixed(1)}%`;
    document.getElementById('riskDiversification').textContent = `${r.diversificationIndex.toFixed(0)} / 100`;
}

function renderHoldingsTable(positions) {
    const tbody = document.getElementById('holdingsBody');
    const badge = document.getElementById('holdingsCountBadge');
    badge.textContent = `${positions.length} Positions Active`;

    if (positions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" class="text-center py-4 text-muted">No open positions. Use the Trade Execution panel to buy stocks.</td></tr>`;
        return;
    }

    tbody.innerHTML = positions.map(p => {
        const isUp = p.unrealizedPnL >= 0;
        const sign = isUp ? '+' : '';
        const colClass = isUp ? 'text-green' : 'text-red';

        return `
            <tr>
                <td><span class="sym-title" onclick="selectStock('${p.symbol}')" style="cursor:pointer">${p.symbol}</span></td>
                <td><small class="text-secondary">${p.companyName}</small></td>
                <td><strong>${p.shares}</strong></td>
                <td>$${p.averageBuyPrice.toFixed(2)}</td>
                <td>$${p.currentPrice.toFixed(2)}</td>
                <td>$${p.marketValue.toFixed(2)}</td>
                <td>$${p.costBasis.toFixed(2)}</td>
                <td class="${colClass}"><strong>${sign}$${p.unrealizedPnL.toFixed(2)} (${sign}${p.unrealizedPnLPercent.toFixed(2)}%)</strong></td>
                <td>${p.portfolioWeight.toFixed(1)}%</td>
                <td>
                    <button class="btn btn-outline-danger btn-sm" onclick="quickSell('${p.symbol}', ${p.shares})">
                        Sell All
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function renderHistoryTable(orders) {
    const tbody = document.getElementById('historyBody');
    if (!orders || orders.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-muted">No trade history recorded yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = orders.map(o => {
        const isBuy = o.orderType === 'BUY';
        const typeBadge = isBuy
            ? '<span class="badge text-green" style="background:rgba(16,185,129,0.15)">BUY</span>'
            : (o.orderType === 'SELL' ? '<span class="badge text-red" style="background:rgba(239,68,68,0.15)">SELL</span>' : '<span class="badge text-cyan">DEPOSIT</span>');

        const statusClass = o.status === 'FILLED' || o.status === 'COMPLETED' ? 'text-green' : 'text-red';

        return `
            <tr>
                <td><code>${o.orderId}</code></td>
                <td><small class="text-muted">${o.timestamp}</small></td>
                <td>${typeBadge}</td>
                <td><strong>${o.symbol}</strong></td>
                <td>${o.quantity}</td>
                <td>$${o.price.toFixed(2)}</td>
                <td>$${o.totalAmount.toFixed(2)}</td>
                <td><span class="${statusClass}">${o.status}</span></td>
            </tr>
        `;
    }).join('');
}

// ----------------- Trade Execution Logic -----------------

function switchOrderType(type) {
    currentOrderType = type;
    const tabBuy = document.getElementById('tabBuy');
    const tabSell = document.getElementById('tabSell');
    const btnSubmit = document.getElementById('btnExecuteTrade');

    if (type === 'BUY') {
        tabBuy.classList.add('active');
        tabSell.classList.remove('active');
        btnSubmit.className = 'btn btn-primary btn-block';
        btnSubmit.innerHTML = `<i class="fa-solid fa-arrow-right-arrow-left"></i> Execute BUY Order`;
    } else {
        tabSell.classList.add('active');
        tabBuy.classList.remove('active');
        btnSubmit.className = 'btn btn-outline-danger btn-block';
        btnSubmit.innerHTML = `<i class="fa-solid fa-arrow-right-arrow-left"></i> Execute SELL Order`;
    }
    calculateOrderEstimate();
}

function calculateOrderEstimate() {
    const qtyInput = document.getElementById('orderQty');
    const qty = parseInt(qtyInput.value) || 0;
    const price = selectedQuote ? selectedQuote.currentPrice : 0;
    const total = qty * price;

    document.getElementById('orderTotalCost').textContent = `$${total.toFixed(2)}`;

    // Available info label
    const label = document.getElementById('tradeAvailableLabel');
    if (currentOrderType === 'BUY') {
        const cash = portfolioSummaryData ? portfolioSummaryData.cashBalance : 0;
        label.textContent = `Buying Power: $${cash.toFixed(2)}`;
    } else {
        const pos = portfolioSummaryData && portfolioSummaryData.positions ?
            portfolioSummaryData.positions.find(p => p.symbol === currentSymbol) : null;
        const shares = pos ? pos.shares : 0;
        label.textContent = `Owned: ${shares} shares`;
    }
}

function setQuickQty(delta) {
    const input = document.getElementById('orderQty');
    let current = parseInt(input.value) || 0;
    input.value = Math.max(1, current + delta);
    calculateOrderEstimate();
}

function setMaxQty() {
    const input = document.getElementById('orderQty');
    const price = selectedQuote ? selectedQuote.currentPrice : 1;

    if (currentOrderType === 'BUY') {
        const cash = portfolioSummaryData ? portfolioSummaryData.cashBalance : 0;
        input.value = Math.max(1, Math.floor(cash / price));
    } else {
        const pos = portfolioSummaryData && portfolioSummaryData.positions ?
            portfolioSummaryData.positions.find(p => p.symbol === currentSymbol) : null;
        input.value = pos ? pos.shares : 0;
    }
    calculateOrderEstimate();
}

async function handleTradeSubmit(e) {
    e.preventDefault();
    const qty = parseInt(document.getElementById('orderQty').value);
    if (!qty || qty <= 0) {
        showToast('Quantity must be greater than 0', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/trade/execute`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                symbol: currentSymbol,
                orderType: currentOrderType,
                quantity: qty
            })
        });

        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            await refreshPortfolioAndRisk();
            await loadTransactionHistory();
            calculateOrderEstimate();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Network error executing trade', 'error');
        console.error(err);
    }
}

function quickSell(symbol, shares) {
    selectStock(symbol);
    switchOrderType('SELL');
    document.getElementById('orderQty').value = shares;
    calculateOrderEstimate();
    window.scrollTo({ top: 300, behavior: 'smooth' });
}

// ----------------- Account Management -----------------

async function resetPortfolio() {
    if (!confirm('Are you sure you want to reset your portfolio to the initial $100,000 cash balance and default test assets?')) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/portfolio/reset`, { method: 'POST' });
        const data = await res.json();
        showToast(data.message || 'Portfolio reset.', 'success');
        await fetchAllData();
    } catch (err) {
        showToast('Failed to reset portfolio.', 'error');
    }
}

function setDepositValue(val) {
    document.getElementById('depositAmount').value = val;
}

async function submitDeposit() {
    const input = document.getElementById('depositAmount');
    const amount = parseFloat(input.value);
    if (!amount || amount <= 0) {
        showToast('Please enter a valid deposit amount', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/api/portfolio/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ amount })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            closeModal('depositModal');
            await refreshPortfolioAndRisk();
            await loadTransactionHistory();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast('Failed to deposit funds', 'error');
    }
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// ----------------- Toast Notifications -----------------

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    const icon = type === 'success' ? 'fa-circle-check text-green' : 'fa-circle-xmark text-red';
    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

