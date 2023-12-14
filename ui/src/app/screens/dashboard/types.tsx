import dayjs, {Dayjs} from "dayjs";

export const OrdersChartTypes = [
    "AOV",
    "Total Sales",
    "Items Sold",
]

export const DateRanges: Map<string, Dayjs> = new Map([
    ["Last 24 Hours", dayjs().subtract(1, "day")],
    ["Last 7 days", dayjs().subtract(1, "week")],
    ["Last month", dayjs().subtract(1, "month")],
    ["Last quarter", dayjs().subtract(3, "month")],
    ["Last year", dayjs().subtract(1, "year")]
])


export const CustomersChartTypes = [
    "New Customers",
    "Active Customers",
    "Lifetime Value",
    "Customers Location"
]