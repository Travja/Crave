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
    import {page} from "$app/stores";
    import {slide} from "svelte/transition";
    import StoreSelector from "$lib/ui/StoreSelector.svelte";
    import {title} from "$lib/variables";
    import {goto} from "$app/navigation";
    import {onMount} from "svelte";

    title.set("Post a Sale");

    let upc = $page.url.searchParams.get("item");
    let store = $page.url.searchParams.get("store");

    let selector, saleForm, newPrice;

    let startDate = new Date().toISOString().split("T")[0], endDate;
    $: console.log("End Date", endDate);

    let rows = 0;
    let numRegex = /^[0-9]*?\.?[0-9]{0,2}$/;

    let items = [];

    $: while (rows > items.length) {
        items.push({
            name: "",
            upc: "",
            price: ""
        });
    }

    $: while (rows < items.length) {
        items.pop();
    }

    onMount(async () => {
        rows = 1;
        items = [{
            name: upc ? await fetchName(upc) : "",
            upc,
            price: ""
        }]
    });

    const submit = () => {
        let stor = selector.getSelectedStore();
        console.log(stor);
        let valid = !!stor;
        console.log(valid);
        if (valid)
            items.forEach(itm => {
                itm.store = stor;
                itm.startDate = startDate;
                itm.endDate = endDate;

                console.log(itm.price)

                if (!(startDate && endDate && new Date(endDate) > new Date()) || !itm.price || itm.price == "")
                    valid = false;
            });

        if (valid) {
            fetch(saleForm.action, {
                headers: {
                    "Content-Type": "application/json"
                },
                method: saleForm.method.toUpperCase(),
                body: JSON.stringify({
                    sales: items
                })
            })
                .then(res => res.json())
                .then(data => {
                    console.log(data);
                    if (data.code == 200) {
                        alert("Sale successfully posted.")
                        goto("/items");
                    }
                })
                .catch(error => console.log({error: true, message: error}));
        } else {
            alert("Please make sure all data is entered correctly.");
        }
    };

    const fetchName = async (upc) => {
        const url = `${gateway()}/item-service/items/name/${upc}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json()
            console.log(json);
            if (json.code == 200) {
                name = json.name;
                return name;
            }

            return "";
        }

        return "";
    };

    const checkUpdate = async (upc, index) => {
        if (upc && upc.length == 12 && index >= 0) {
            items[index].name = await fetchName(upc);
        }
    };

    const testInput = e => {
        if (!numRegex.test(e.target.value + e.key))
            e.preventDefault();
    };

</script>

<div class="parent full">
    <form action="{gateway()}/sale-service/sale" bind:this={saleForm} class="wrapper"
          id="saleForm" method="post">
        <p>Please enter the sale information for the item and upload an applicable advertisement so we can verify
            that the sale is legitimate!</p>

        <hr/>

        <StoreSelector bind:this={selector} {store}/>

        <div class="dates">
            <label for="startDate">Start Date
                <input bind:value={startDate} id="startDate" name="endDate" type="date"/>
            </label>
            <label for="startDate">End Date
                <input bind:value={endDate} id="endDate" name="endDate" type="date"/>
            </label>
        </div>

        {#if rows > 0}
            <div id="table">
                {#each Array(rows).fill(1).map((n, i) => i) as r (r)}
                    <div class="row" transition:slide={{duration: 250}}>
                        <div><input id="sale-{r}-name" name="sale-{r}-name" placeholder="Item Name" type="text"
                                    bind:value={items[r].name}
                        /></div>
                        <div><input id="sale-{r}-upc" name="sale-{r}-upc" placeholder="Item UPC" type="text"
                                    bind:value={items[r].upc}
                                    on:keyup={e => checkUpdate(items[r].upc, r)}
                        />
                        </div>
                        <div><input id="sale-{r}-price" class="price" name="sale-{r}-price"
                                    placeholder="Sale Price"
                                    type="number"
                                    min="0.01" step="0.01"
                                    bind:value={items[r].price}
                                    on:keypress={testInput}
                        /></div>
                    </div>
                {/each}
            </div>
        {/if}

        <div class="buttons">
            <div class="button" on:click={() => rows++}>Add Row</div>
            <div class="button" on:click={() => {if(rows > 1) rows--;}}>Remove Row</div>
            <label class="button" for="ad">Upload Sale Advertisement</label>
            <div class="button publish" on:click={submit}>Publish Sale</div>
            <input id="ad" name="ad" type="file"/>
        </div>

    </form>
</div>

<style>

    .buttons {
        display: flex;
        flex-direction: row;
    }

    .buttons .button {
        margin: 0.5em 0.25em;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
    }

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

    input[type="file"] {
        display: none;
    }

    .publish {
        background: var(--submit-color);
    }

    .dates {
        display: flex;
        flex-direction: row;
        margin-bottom: 1rem;
    }

    .dates label {
        display: flex;
        flex-direction: column;
        margin: 0 1rem;
    }

    #table {
        border: 1px solid var(--fg-color);
    }

    #table * {
        font-size: 1rem;
    }

    .row {
        align-items: stretch;
        border-bottom: 1px solid black;
    }

    .row:last-child {
        border-bottom: none;
    }

    .row, .row * {
        display: flex;
        flex-grow: 1;
        padding: 0;
        margin: 0;
    }

    .row > * {
        align-items: center;
        flex-direction: column;
        padding: 5px 0;
    }

    .row:nth-child(even) {
        background: #ddd;
    }

    .row input {
        padding: 0 5px;
        border: none;
        border-left: 1px solid rgba(0, 0, 0, 0.5);
        background: transparent;
    }

    .row input:focus {
        outline: none;
    }

    #table label {
        margin: 0 10px 0 5px;
    }

    .button.submit {
        background: var(--accent-color);
    }

    input::-webkit-outer-spin-button,
    input::-webkit-inner-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }

    input[type=number] {
        -moz-appearance: textfield;
    }

    .row div:first-child input {
        border-left: none;
    }
</style>
