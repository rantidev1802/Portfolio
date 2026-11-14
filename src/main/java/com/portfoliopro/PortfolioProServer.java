package com.portfoliopro;

import com.portfoliopro.model.*;
import com.portfoliopro.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Standalone Zero-Dependency Java HTTP Server.
 * Runs on standard Java without requiring external dependencies or Maven.
 */
public class PortfolioProServer {

    private static final int PORT = 8080;
    private static final MarketDataService marketDataService = new MarketDataService();
    private static final TechnicalAnalysisService technicalAnalysisService = new TechnicalAnalysisService(marketDataService);
    private static final TradingService tradingService = new TradingService(marketDataService);
    private static final PortfolioService portfolioService = new PortfolioService(tradingService, marketDataService);
    private static final RiskManagementService riskManagementService = new RiskManagementService(portfolioService, marketDataService);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        // API Contexts
        server.createContext("/api/market/quotes", new QuotesHandler());
        server.createContext("/api/market/quote/", new SingleQuoteHandler());
        server.createContext("/api/market/history/", new HistoryHandler());
        server.createContext("/api/market/analysis/", new AnalysisHandler());
        server.createContext("/api/market/fundamental/", new FundamentalHandler());

        server.createContext("/api/trade/execute", new TradeExecuteHandler());
        server.createContext("/api/trade/history", new TradeHistoryHandler());

        server.createContext("/api/portfolio/summary", new PortfolioSummaryHandler());
        server.createContext("/api/portfolio/allocations", new AllocationsHandler());
        server.createContext("/api/portfolio/risk", new RiskHandler());
        server.createContext("/api/portfolio/reset", new ResetHandler());
        server.createContext("/api/portfolio/deposit", new DepositHandler());

        // Static file handler (serves HTML/CSS/JS frontend)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("======================================================================");
        System.out.println("  PortfolioPro: Stock Trading & Portfolio Management Platform         ");
        System.out.println("  Java Standalone HTTP Server successfully started on port " + PORT + "  ");
        System.out.println("  Open your browser at: http://localhost:" + PORT + "                    ");
        System.out.println("======================================================================");
    }

    // ----------------- Handlers -----------------

    static class QuotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                return;
            }
            List<StockQuote> quotes = marketDataService.getAllQuotes();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < quotes.size(); i++) {
                sb.append(quoteToJson(quotes.get(i)));
                if (i < quotes.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class SingleQuoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            String path = exchange.getRequestURI().getPath();
            String sym = path.substring(path.lastIndexOf('/') + 1);
            StockQuote quote = marketDataService.getQuote(sym);
            if (quote == null) {
                sendJson(exchange, 404, "{\"error\":\"Symbol not found\"}");
            } else {
                sendJson(exchange, 200, quoteToJson(quote));
            }
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            String path = exchange.getRequestURI().getPath();
            String sym = path.substring(path.lastIndexOf('/') + 1);
            List<HistoricalBar> bars = marketDataService.getHistory(sym);
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < bars.size(); i++) {
                HistoricalBar b = bars.get(i);
                sb.append(String.format("{\"date\":\"%s\",\"open\":%.2f,\"high\":%.2f,\"low\":%.2f,\"close\":%.2f,\"volume\":%d}",
                        b.getDate(), b.getOpen(), b.getHigh(), b.getLow(), b.getClose(), b.getVolume()));
                if (i < bars.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class AnalysisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            String path = exchange.getRequestURI().getPath();
            String sym = path.substring(path.lastIndexOf('/') + 1);
            TechnicalIndicators ind = technicalAnalysisService.computeIndicators(sym);
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"symbol\":\"").append(ind.getSymbol()).append("\",");
            sb.append("\"currentPrice\":").append(ind.getCurrentPrice()).append(",");
            sb.append("\"sma20\":").append(ind.getSma20()).append(",");
            sb.append("\"sma50\":").append(ind.getSma50()).append(",");
            sb.append("\"ema12\":").append(ind.getEma12()).append(",");
            sb.append("\"ema26\":").append(ind.getEma26()).append(",");
            sb.append("\"rsi14\":").append(ind.getRsi14()).append(",");
            sb.append("\"macd\":").append(ind.getMacd()).append(",");
            sb.append("\"macdSignal\":").append(ind.getMacdSignal()).append(",");
            sb.append("\"macdHistogram\":").append(ind.getMacdHistogram()).append(",");
            sb.append("\"bollingerUpper\":").append(ind.getBollingerUpper()).append(",");
            sb.append("\"bollingerMiddle\":").append(ind.getBollingerMiddle()).append(",");
            sb.append("\"bollingerLower\":").append(ind.getBollingerLower()).append(",");
            sb.append("\"signal\":\"").append(ind.getSignal()).append("\",");
            sb.append("\"rsiSignal\":\"").append(escapeJson(ind.getRsiSignal())).append("\",");
            sb.append("\"macdSignalInterpretation\":\"").append(escapeJson(ind.getMacdSignalInterpretation())).append("\",");

            sb.append("\"rsiSeries\":[");
            if (ind.getRsiSeries() != null) {
                for (int i = 0; i < ind.getRsiSeries().size(); i++) {
                    sb.append(ind.getRsiSeries().get(i));
                    if (i < ind.getRsiSeries().size() - 1) sb.append(",");
                }
            }
            sb.append("],");

            sb.append("\"sma20Series\":[");
            if (ind.getSma20Series() != null) {
                for (int i = 0; i < ind.getSma20Series().size(); i++) {
                    sb.append(ind.getSma20Series().get(i));
                    if (i < ind.getSma20Series().size() - 1) sb.append(",");
                }
            }
            sb.append("]");
            sb.append("}");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class FundamentalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            String path = exchange.getRequestURI().getPath();
            String sym = path.substring(path.lastIndexOf('/') + 1);
            FundamentalData f = marketDataService.getFundamental(sym);
            if (f == null) {
                sendJson(exchange, 404, "{\"error\":\"Not found\"}");
                return;
            }
            String json = String.format(Locale.US,
                    "{\"symbol\":\"%s\",\"companyName\":\"%s\",\"sector\":\"%s\",\"industry\":\"%s\",\"marketCap\":\"%s\",\"peRatio\":%.2f,\"forwardPe\":%.2f,\"eps\":%.2f,\"dividendYield\":%.2f,\"fiftyTwoWeekHigh\":%.2f,\"fiftyTwoWeekLow\":%.2f,\"beta\":%.2f,\"targetPrice\":%.2f,\"analystRating\":\"%s\",\"description\":\"%s\"}",
                    f.getSymbol(), f.getCompanyName(), f.getSector(), f.getIndustry(), f.getMarketCap(),
                    f.getPeRatio(), f.getForwardPe(), f.getEps(), f.getDividendYield(), f.getFiftyTwoWeekHigh(),
                    f.getFiftyTwoWeekLow(), f.getBeta(), f.getTargetPrice(), f.getAnalystRating(), escapeJson(f.getDescription()));
            sendJson(exchange, 200, json);
        }
    }

    static class TradeExecuteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            Map<String, String> params = parseSimpleJson(body);
            String symbol = params.get("symbol");
            String orderType = params.get("orderType");
            String qtyStr = params.get("quantity");

            if (symbol == null || orderType == null || qtyStr == null) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"symbol, orderType, quantity required\"}");
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyStr.trim());
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"Invalid quantity\"}");
                return;
            }

            TradeExecutionResponse res = tradingService.executeOrder(symbol, orderType, qty);
            String orderJson = (res.getOrder() != null) ? tradeOrderToJson(res.getOrder()) : "null";
            String json = String.format(Locale.US,
                    "{\"success\":%b,\"message\":\"%s\",\"newCashBalance\":%.2f,\"order\":%s}",
                    res.isSuccess(), escapeJson(res.getMessage()), res.getNewCashBalance(), orderJson);
            sendJson(exchange, 200, json);
        }
    }

    static class TradeHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            List<TradeOrder> list = tradingService.getTransactionHistory();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(tradeOrderToJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class PortfolioSummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            PortfolioSummary s = portfolioService.getPortfolioSummary();
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append(String.format(Locale.US,
                    "\"totalPortfolioValue\":%.2f,\"cashBalance\":%.2f,\"investedValue\":%.2f,\"totalUnrealizedPnL\":%.2f,\"totalUnrealizedPnLPercent\":%.2f,\"totalRealizedPnL\":%.2f,\"dailyPnL\":%.2f,\"dailyPnLPercent\":%.2f,\"totalPositions\":%d,\"positions\":[",
                    s.getTotalPortfolioValue(), s.getCashBalance(), s.getInvestedValue(),
                    s.getTotalUnrealizedPnL(), s.getTotalUnrealizedPnLPercent(), s.getTotalRealizedPnL(),
                    s.getDailyPnL(), s.getDailyPnLPercent(), s.getTotalPositions()));

            List<Position> pos = s.getPositions();
            for (int i = 0; i < pos.size(); i++) {
                Position p = pos.get(i);
                sb.append(String.format(Locale.US,
                        "{\"symbol\":\"%s\",\"companyName\":\"%s\",\"shares\":%d,\"averageBuyPrice\":%.2f,\"currentPrice\":%.2f,\"marketValue\":%.2f,\"costBasis\":%.2f,\"unrealizedPnL\":%.2f,\"unrealizedPnLPercent\":%.2f,\"portfolioWeight\":%.2f}",
                        p.getSymbol(), p.getCompanyName(), p.getShares(), p.getAverageBuyPrice(),
                        p.getCurrentPrice(), p.getMarketValue(), p.getCostBasis(),
                        p.getUnrealizedPnL(), p.getUnrealizedPnLPercent(), p.getPortfolioWeight()));
                if (i < pos.size() - 1) sb.append(",");
            }
            sb.append("]}");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class AllocationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            List<AssetAllocation> list = portfolioService.getAssetAllocations();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                AssetAllocation a = list.get(i);
                sb.append(String.format(Locale.US, "{\"label\":\"%s\",\"value\":%.2f,\"percentage\":%.2f,\"color\":\"%s\"}",
                        a.getLabel(), a.getValue(), a.getPercentage(), a.getColor()));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendJson(exchange, 200, sb.toString());
        }
    }

    static class RiskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            RiskMetrics r = riskManagementService.calculateRiskMetrics();
            String json = String.format(Locale.US,
                    "{\"portfolioBeta\":%.2f,\"sharpeRatio\":%.2f,\"var95Dollar\":%.2f,\"var95Percent\":%.2f,\"annualizedVolatility\":%.2f,\"maxDrawdown\":%.2f,\"riskLevel\":\"%s\",\"diversificationIndex\":%.2f}",
                    r.getPortfolioBeta(), r.getSharpeRatio(), r.getVar95Dollar(), r.getVar95Percent(),
                    r.getAnnualizedVolatility(), r.getMaxDrawdown(), r.getRiskLevel(), r.getDiversificationIndex());
            sendJson(exchange, 200, json);
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            tradingService.resetPortfolio();
            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Portfolio successfully reset.\"}");
        }
    }

    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { sendCors(exchange); return; }
            String body = readBody(exchange);
            Map<String, String> map = parseSimpleJson(body);
            double amount = 10000.0;
            if (map.containsKey("amount")) {
                try {
                    amount = Double.parseDouble(map.get("amount"));
                } catch (Exception ignored) {}
            }
            tradingService.depositCash(amount);
            sendJson(exchange, 200, String.format(Locale.US, "{\"success\":true,\"message\":\"Deposited $%.2f\",\"newBalance\":%.2f}", amount, tradingService.getCashBalance()));
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.isEmpty()) {
                path = "/index.html";
            }

            Path filePath = Paths.get("src/main/resources/static" + path);
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                // Try from project root or resources
                filePath = Paths.get("resources/static" + path);
            }

            if (!Files.exists(filePath)) {
                String notFound = "<h1>404 Not Found</h1>";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes());
                os.close();
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html";
            else if (path.endsWith(".css")) contentType = "text/css";
            else if (path.endsWith(".js")) contentType = "application/javascript";
            else if (path.endsWith(".json")) contentType = "application/json";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    // ----------------- Helpers -----------------

    private static String quoteToJson(StockQuote q) {
        return String.format(Locale.US,
                "{\"symbol\":\"%s\",\"companyName\":\"%s\",\"currentPrice\":%.2f,\"change\":%.2f,\"changePercent\":%.2f,\"dayHigh\":%.2f,\"dayLow\":%.2f,\"volume\":%d,\"previousClose\":%.2f,\"sector\":\"%s\"}",
                q.getSymbol(), q.getCompanyName(), q.getCurrentPrice(), q.getChange(), q.getChangePercent(),
                q.getDayHigh(), q.getDayLow(), q.getVolume(), q.getPreviousClose(), q.getSector());
    }

    private static String tradeOrderToJson(TradeOrder o) {
        return String.format(Locale.US,
                "{\"orderId\":\"%s\",\"symbol\":\"%s\",\"orderType\":\"%s\",\"quantity\":%d,\"price\":%.2f,\"totalAmount\":%.2f,\"timestamp\":\"%s\",\"status\":\"%s\"}",
                o.getOrderId(), o.getSymbol(), o.getOrderType(), o.getQuantity(), o.getPrice(),
                o.getTotalAmount(), o.getTimestamp(), o.getStatus());
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void sendCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(204, -1);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int r;
        while ((r = is.read(buf)) != -1) {
            baos.write(buf, 0, r);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        String clean = json.trim().replaceAll("[{}\"]", "");
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}

