export interface ProductDisplay {
    title: string
    subheader?: string
    price: number
    description: string[]
    buttonText: string
    buttonVariant: string
}

export const productsDisplay: ProductDisplay[] = [
    {
        title: 'Lorem ipsum B',
        price: 10,
        description: [
            'Lorem ipsum ',
            'dolor sit amet, ',
            'consectetur adipiscing elit',
        ],
        buttonText: 'Buy',
        buttonVariant: 'outlined',
    },
    {
        title: 'Lorem ipsum A',
        subheader: 'Most popular',
        price: 50,
        description: [
            'Lorem ipsum ',
            'dolor sit amet, ',
            'consectetur adipiscing elit',
        ],
        buttonText: 'Buy',
        buttonVariant: 'contained',
    },
    {
        title: 'Lorem ipsum C',
        price: 100,
        description: [
            'Lorem ipsum ',
            'dolor sit amet, ',
            'consectetur adipiscing elit',
        ],
        buttonText: 'Buy',
        buttonVariant: 'outlined',
    },
];

export interface Product {
    name: string
    price: number
}

export const Products: Product[] = productsDisplay.map(pd => {
    return {
        name: pd.title, price: pd.price
    }
})