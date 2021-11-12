<script context="module">
    import {gateway} from "$lib/variables";

    let name = "";

    export async function load({page, fetch}) {
        if (page.query.get("item")) {
            const url = `${gateway()}/item-service/items/name/${page.query.get("item")}`;
            console.log(url);
            const res = await fetch(url);

            if (res.ok) {
                let json = await res.json()
                console.log(json);
                name = json.name;
                return name;
            }

            console.log(res);
        }

        return true;
    }
</script>

<script>
    //TODO Need frontend start/end dates.
    import {page} from "$app/stores";
    import {slide} from "svelte/transition";
    import StoreSelector from "$lib/ui/StoreSelector.svelte";
    import {title} from "$lib/variables";

    title.set("Create a Sale");

    let upc = $page.query.get("item");
    let store = $page.query.get("store");

    let name;
    let selector, upcInput, saleForm, newPrice;

    const submit = () => {
        console.log(selector.getSelectedStore());
        fetch(saleForm.action, {
            headers: {
                "Content-Type": "application/json"
            },
            method: saleForm.method.toUpperCase(),
            body: JSON.stringify({
                item: upcInput.value,
                store: selector.getSelectedStore(),
                newPrice,
                startDate: "12-1-2021",
                endDate: "12-9-2021"
            })
        })
            .then(res => res.json())
            .then(data => {
                console.log(data);
            })
            .catch(error => console.log({error: true, message: error}));
    };

    const fetchName = async () => {
        const url = `${gateway()}/item-service/items/name/${upcInput.value}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json()
            console.log(json);
            name = json.name;
            return name;
        }
    };

    const checkUpdate = () => {
        if (upcInput && upcInput.value.length == 12) {
            fetchName();
        }
    };

</script>

<div class="parent full">
    <form action="{gateway()}/sale-service/sale" bind:this={saleForm} class="wrapper"
          id="saleForm" method="post">
        <p>Please enter the sale information for the item and upload an applicable advertisement so we can verify
            that the sale is legitimate!</p>

        <hr/>

        <StoreSelector bind:this={selector} {store}/>

        <label for="upc">UPC</label>
        <input bind:this={upcInput} disabled="{!!upc}" id="upc" name="upc" on:keyup={checkUpdate} placeholder="UPC"
               type="number"
               value="{upc}"/>

        {#if name}
            <div class="name" transition:slide>{name}</div>
        {/if}

        <label for="price">Price</label>
        <input bind:value={newPrice} id="price" min="0" name="price" placeholder="Sale Price" step="0.01"
               type="number"/>

        <label class="button" for="ad">Upload Sale Advertisement</label>
        <input id="ad" name="ad" type="file"/>
        <div class="button publish" on:click={submit}>Publish Sale</div>
    </form>
</div>

<style>
    .parent {
        margin: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
    }

    hr {
        width: 90%;
    }

    .wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
    }

    .name {
        font-size: 0.7rem;
    }

    input[type="file"] {
        display: none;
    }

    .publish {
        background: var(--submit-color);
    }
</style>