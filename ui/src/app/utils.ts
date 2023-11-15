import {PhoneNumber} from "../generated/com/example/common/domain/address_pb";

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