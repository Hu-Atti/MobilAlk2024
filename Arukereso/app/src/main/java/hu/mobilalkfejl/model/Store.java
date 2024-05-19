    package hu.mobilalkfejl.model;
    public class Store {
        private String id;
        private String name;
        private String address;
        private String logo;
        private int shippingCost;
        private float rating;
        public Store() {
        }
        public Store(String name, String address, String logo, int shippingCost, float rating) {
            this.name = name;
            this.address = address;
            this.logo = logo;
            this.shippingCost = shippingCost;
            this.rating = rating;
        }

        public float getRating() {
            return rating;
        }
        public void setRating(float rating) {
            this.rating = rating;
        }
        public String _getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getAddress() {
            return address;
        }
        public void setAddress(String address) {
            this.address = address;
        }
        public String getLogo() {
            return logo;
        }
        public void setLogo(String logo) {
            this.logo = logo;
        }
        public int getShippingCost() {
            return shippingCost;
        }
        public void setShippingCost(int shippingCost) {
            this.shippingCost = shippingCost;
        }
    }
