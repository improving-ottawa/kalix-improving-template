import {PhoneNumber} from "../generated/com/example/common/domain/address_pb";

export function getWindowDimensions() {
    const { innerWidth: width, innerHeight: height } = window;
    return {
        width,
        height
    };
}
function isLargeViewport() {
    const width = getWindowDimensions().width;
    return width >= 1440 && width <= 1920;
}

function isMediumViewport() {
    const width = getWindowDimensions().width;
    return width >= 905 && width <= 1239;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function isSmallViewport() {
    const width = getWindowDimensions().width;
    return width >= 0 && width <= 599;
}

export function getStyleForViewport(styleType: string = "body") {
    if(!["body", "title", "headline"].includes(styleType))
        styleType = "body"
    if(isMediumViewport()) {
        return `large-${styleType}`
    } else if(isLargeViewport()) {
        return `medium-${styleType}`
    } else {
        return `small-${styleType}`
    }
}

export function getViewport() {
    if(isMediumViewport()) {
        return "large-viewport"
    } else if(isLargeViewport()) {
        return "medium-viewport"
    } else {
        return "small-viewport"
    }
}

export function parsePhoneNumber(phoneNumberStr: string): PhoneNumber {
    const phoneNumber = new PhoneNumber()

    phoneNumber.setCountryCode(phoneNumberStr.slice(0, 1))
    phoneNumber.setAreaCode(phoneNumberStr.slice(1, 4))
    phoneNumber.setPersonalNumber(phoneNumberStr.slice(4, 7) + phoneNumberStr.slice(7))

    return phoneNumber
}

export function printPhoneNumber(phoneNumber: PhoneNumber | undefined): string {
    return phoneNumber ? phoneNumber.getCountryCode() + "-(" + phoneNumber.getAreaCode() + ")-" + phoneNumber.getPersonalNumber().substring(0, 3) + "-" + phoneNumber.getPersonalNumber().substring(3)
        : "1"
}

// Use this for modifying lists in state based on an index provided by UX components that correlates to the data in state
export type IndexedListItem<T> = {
    index: number,
    item: T
}